package com.aamo.exercisetracker.features.exercise

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.RoutineDatabase
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.services.CountDownTimerService
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.components.SegmentedCircularProgressIndicator
import com.aamo.exercisetracker.utility.extensions.date.toClockString
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.extensions.general.onFalse
import com.aamo.exercisetracker.utility.extensions.general.onNotNull
import com.aamo.exercisetracker.utility.extensions.general.onNull
import com.aamo.exercisetracker.utility.extensions.general.onTrue
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import com.aamo.exercisetracker.utility.tags.ERROR_TAG
import com.aamo.exercisetracker.utility.viewmodels.ViewModelState
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Calendar
import kotlin.concurrent.timer
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Serializable
data class ExerciseScreen(val id: Long = 0)

class ExerciseScreenViewModel(
  private val fetchData: suspend () -> Model,
  private val saveProgress: suspend () -> Unit,
) : ViewModel() {
  data class Model(
    val exerciseName: String,
    val sets: List<SetModel>,
  ) {
    data class SetModel(
      val repetitions: Int?,
      val setDuration: Duration?,
      val restDuration: Duration?,
      val unit: String
    )
  }

  class TimerState(val duration: Duration) {
    val isActive = ViewModelState(false).validation { value -> value && duration > 0.seconds }
  }

  class UiState {
    inner class SetState(val index: Int) {
      val set = sets.elementAtOrNull(index)
      val setTimer: TimerState? = set?.setDuration?.let { TimerState(duration = it) }
      val restTimer: TimerState? = set?.restDuration?.let { TimerState(duration = it) }
    }

    var exerciseName by mutableStateOf(String.EMPTY)
    var sets: List<Model.SetModel> = emptyList()
    val setState = ViewModelState(SetState(0)).onChange { inProgress = it.index != 0 }
    var isLoading by mutableStateOf(true)
    var inProgress by mutableStateOf(false)
      private set
  }

  val uiState = UiState()

  init {
    viewModelScope.launch {
      fetchData().let { result ->
        uiState.apply {
          exerciseName = result.exerciseName
          sets = result.sets
          setState.update(SetState(index = 0))
          isLoading = false
        }
      }
    }
  }

  fun startSet(countDownTimerService: CountDownTimerService) {
    uiState.setState.value.setTimer.onNotNull { setTimer ->
      startTimer(
        countDownTimerService = countDownTimerService,
        duration = setTimer.duration,
        onStart = { setTimer.isActive.update(true) },
        onFinished = { finishSet(countDownTimerService) },
        onCleanUp = { setTimer.isActive.update(false) })
    }.onNull {
      finishSet(countDownTimerService)
    }
  }

  fun finishExercise() {
    viewModelScope.launch {
      runCatching { saveProgress() }.onFailure { error ->
        Log.e(ERROR_TAG, error.message.toString())
      }
    }
  }

  fun stopTimer(countDownTimerService: CountDownTimerService) {
    countDownTimerService.stop()
  }

  fun cancelTimer(countDownTimerService: CountDownTimerService) {
    countDownTimerService.cancel()
  }

  private fun finishSet(countDownTimerService: CountDownTimerService) {
    uiState.apply {
      SetState(index = setState.value.index + 1).let { nextSetState ->
        nextSetState.set.onNull {
          // Was final set, no resting
          setState.update(nextSetState)
        }.onNotNull { set ->
          setState.value.restTimer.onNotNull { restTimer ->
            startTimer(
              countDownTimerService = countDownTimerService,
              duration = restTimer.duration,
              onStart = { restTimer.isActive.update(true) },
              onFinished = { setState.update(nextSetState) },
              onCleanUp = { restTimer.isActive.update(false) })
          }
        }
      }
    }
  }

  private fun startTimer(
    countDownTimerService: CountDownTimerService,
    duration: Duration,
    onFinished: () -> Unit,
    onStart: (() -> Unit)? = null,
    onCleanUp: (() -> Unit)? = null,
  ) {
    if (duration <= 0.seconds) {
      onStart?.invoke()
      onFinished()
      onCleanUp?.invoke()
      return
    }

    countDownTimerService.start(
      durationMillis = duration.inWholeMilliseconds,
      onFinished = onFinished,
      onStart = onStart,
      onCleanUp = onCleanUp
    )
  }
}

fun NavGraphBuilder.exerciseScreen(onBack: () -> Unit, onEdit: (id: Long) -> Unit) {
  composable<ExerciseScreen> { navStack ->
    val (exerciseId) = navStack.toRoute<ExerciseScreen>()
    val setTimerTitle = stringResource(R.string.title_set_timer)
    val restTimerTitle = stringResource(R.string.title_rest_timer)
    val context = LocalContext.current
    val dao = RoutineDatabase.getDatabase(context.applicationContext).routineDao()

    val viewmodel: ExerciseScreenViewModel = viewModel(factory = viewModelFactory {
      initializer {
        ExerciseScreenViewModel(fetchData = {
          (dao.getExerciseWithProgressAndSets(exerciseId)
            ?: throw Exception("Failed to fetch data")).let { result ->
            ExerciseScreenViewModel.Model(
              exerciseName = result.exercise.name, sets = result.sets.map { set ->
                ExerciseScreenViewModel.Model.SetModel(
                  repetitions = ifElse(
                    condition = set.valueType == ExerciseSet.ValueType.REPETITION,
                    ifTrue = { set.value },
                    ifFalse = { null }),
                  setDuration = ifElse(
                    condition = set.valueType == ExerciseSet.ValueType.COUNTDOWN,
                    ifTrue = { set.value.milliseconds },
                    ifFalse = { null }),
                  restDuration = result.exercise.restDuration,
                  unit = set.unit
                )
              })
          }
        }, saveProgress = {
          dao.upsert(Calendar.getInstance().time.let { finishedDate ->
            dao.getExerciseProgressByExerciseId(exerciseId)?.copy(finishedDate = finishedDate)
              ?: ExerciseProgress(exerciseId = exerciseId, finishedDate = finishedDate)
          }).also {
            onBack()
          }
        })
      }
    })
    val uiState = viewmodel.uiState
    var timerService: CountDownTimerService? by remember { mutableStateOf(null) }
    val connection = remember {
      object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
          timerService = (binder as? CountDownTimerService.BinderHelper)?.getService()?.apply {
            // Notifications needs to be hidden also here, because the service will be null in
            //  LifecycleEventEffect ON_RESUME when the configuration changes
            hideNotification()
          }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
          timerService = null
        }
      }
    }

    var openInProgressBackDialog by remember { mutableStateOf(false) }
    var openInProgressEditDialog by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
      timerService?.hideNotification()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
      uiState.setState.value.apply {
        when {
          restTimer?.isActive?.value == true -> timerService?.showNotification(restTimerTitle)
          setTimer?.isActive?.value == true -> timerService?.showNotification(setTimerTitle)
          else -> {}
        }
      }
    }

    BackHandler(enabled = uiState.inProgress) {
      openInProgressBackDialog = true
    }

    if (openInProgressBackDialog) {
      InProgressDialog(onDismiss = { openInProgressBackDialog = false }, onConfirm = {
        openInProgressBackDialog = false
        onBack()
      })
    }
    if (openInProgressEditDialog) {
      InProgressDialog(onDismiss = { openInProgressEditDialog = false }, onConfirm = {
        openInProgressEditDialog = false
        onEdit(exerciseId)
      })
    }

    DisposableEffect(Unit) {
      context.apply {
        bindService(
          Intent(this, CountDownTimerService::class.java), connection, Context.BIND_AUTO_CREATE
        )
      }

      onDispose {
        context.unbindService(connection)
      }
    }

    LoadingScreen(enabled = uiState.isLoading) {
      ExerciseScreen(
        uiState = uiState,
        onBack = {
          uiState.inProgress.onTrue { openInProgressBackDialog = true }.onFalse { onBack() }
        },
        onEdit = {
          uiState.inProgress.onTrue { openInProgressEditDialog = true }
            .onFalse { onEdit(exerciseId) }
        },
        onStartSet = { timerService?.let { viewmodel.startSet(it) } },
        onStopSetTimer = { timerService?.let { viewmodel.stopTimer(it) } },
        onCancelSet = { timerService?.let { viewmodel.cancelTimer(it) } },
        onStopRest = { timerService?.let { viewmodel.stopTimer(it) } },
        onFinishExercise = { viewmodel.finishExercise() },
      )
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ExerciseScreen(
  uiState: ExerciseScreenViewModel.UiState,
  onBack: () -> Unit,
  onEdit: () -> Unit,
  onStartSet: () -> Unit,
  onStopSetTimer: () -> Unit,
  onCancelSet: () -> Unit,
  onStopRest: () -> Unit,
  onFinishExercise: () -> Unit,
) {
  val restTimerSheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true,
    confirmValueChange = { false /* Prevents closing by pressing outside the sheet */ })
  val setTimerSheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true,
    confirmValueChange = { false /* Prevents closing by pressing outside the sheet */ })
  var showRestSheet by remember { mutableStateOf(false) }
  var showSetTimerSheet by remember { mutableStateOf(false) }

  LaunchedEffect(uiState.setState.value.restTimer?.isActive?.value) {
    // sheet state can't be checked with SheetState.currentValue, because confirmValueChange
    //  is always false, so the state will not change when closing the sheet.
    showRestSheet = restTimerSheetState.let { sheetState ->
      uiState.setState.value.restTimer?.isActive?.value.also { active ->
        if (active == true) sheetState.show() else sheetState.hide()
      }
    } ?: false
  }

  LaunchedEffect(uiState.setState.value.setTimer?.isActive?.value) {
    // sheet state can't be checked with SheetState.currentValue, because confirmValueChange
    //  is always false, so the state will not change when closing the sheet.
    showSetTimerSheet = setTimerSheetState.let { sheetState ->
      uiState.setState.value.setTimer?.isActive?.value.also { active ->
        if (active == true) sheetState.show() else sheetState.hide()
      }
    } ?: false
  }

  Scaffold(
    topBar = {
      TopAppBar(title = { Text(uiState.exerciseName) }, actions = {
        IconButton(onClick = onEdit) {
          Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = stringResource(R.string.cd_edit_exercise)
          )
        }
      }, navigationIcon = { BackNavigationIconButton(onBack = onBack) })
    }) { innerPadding ->
    Surface(
      modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize()
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.padding(16.dp)
      ) {
        SetProgressIndicator(
          currentSet = uiState.setState.value.index + 1, totalSets = uiState.sets.size
        )

        if (uiState.setState.value.set != null) {
          SetContent(setState = uiState.setState.value, onStartSet = { onStartSet() })
        }
        else {
          Button(
            shape = CardDefaults.shape,
            onClick = onFinishExercise,
            modifier = Modifier
              .heightIn(max = 100.dp)
              .fillMaxSize()
          ) {
            Text(stringResource(R.string.btn_done), style = MaterialTheme.typography.titleLarge)
          }
        }
      }
    }

    // Visibility needs to be checked with showRestSheet because
    //  otherwise the sheet closing animation will not work correctly.
    TimerSheet(
      isVisible = showSetTimerSheet,
      timerTitle = stringResource(R.string.title_set_timer),
      timerState = uiState.setState.value.setTimer,
      sheetState = setTimerSheetState,
      onDismissRequest = onCancelSet,
    ) {
      Button(
        onClick = onCancelSet, colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error
        ), modifier = Modifier.weight(1f)
      ) {
        Text(stringResource(R.string.btn_cancel))
      }
      Button(
        onClick = onStopSetTimer, colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.secondary
        ), modifier = Modifier.weight(1f)
      ) {
        Text(stringResource(R.string.btn_finish))
      }
    }
  }
  TimerSheet(
    isVisible = showRestSheet,
    timerTitle = stringResource(R.string.title_rest),
    timerState = uiState.setState.value.restTimer,
    sheetState = restTimerSheetState,
    onDismissRequest = onStopRest,
  ) {
    Button(
      onClick = onStopRest, colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary
      )
    ) {
      Text(stringResource(R.string.btn_finish))
    }
  }
}

@Composable
fun SetProgressIndicator(currentSet: Int, totalSets: Int) {
  val progress by remember(currentSet) {
    mutableFloatStateOf((currentSet.toFloat() - 1) / totalSets.toFloat())
  }
  val animatedProgress by animateFloatAsState(
    targetValue = progress, animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
  )

  Box(
    contentAlignment = Alignment.Center, modifier = Modifier
      .fillMaxWidth(.7f)
      .aspectRatio(1f)
  ) {
    SegmentedCircularProgressIndicator(
      progress = { animatedProgress },
      segments = totalSets,
      strokeWidth = 20.dp,
      gapSize = 10.dp,
      strokeCap = StrokeCap.Round,
      modifier = Modifier.fillMaxSize()
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = ifElse(
          condition = currentSet > totalSets,
          ifTrue = { stringResource(R.string.title_completed) },
          ifFalse = { stringResource(R.string.title_set) }),
        style = MaterialTheme.typography.titleMedium
      )
      Text(
        text = "${min(currentSet, totalSets)}/$totalSets",
        style = MaterialTheme.typography.displayLarge,
        textAlign = TextAlign.Center
      )
      Spacer(Modifier.height(with(LocalDensity.current) {
        // Centers the text inside the progress indicator
        MaterialTheme.typography.titleMedium.lineHeight.toDp()
      }))
    }
  }
}

@Composable
fun SetContent(setState: ExerciseScreenViewModel.UiState.SetState, onStartSet: () -> Unit) {
  fun getSetValueString(set: ExerciseScreenViewModel.Model.SetModel?): String {
    return when {
      set?.repetitions != null -> set.repetitions.toString()
      set?.setDuration != null -> set.setDuration.inWholeMinutes.toString()
      else -> String.EMPTY
    }
  }

  if (setState.set == null) return

  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Card {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .padding(24.dp)
          .fillMaxWidth()
      ) {
        Text(
          text = stringResource(R.string.title_current_set),
          style = MaterialTheme.typography.titleMedium
        )
        Text(
          text = "${getSetValueString(setState.set)} ${setState.set.unit}",
          style = MaterialTheme.typography.displayMedium
        )
      }
    }
    Button(
      shape = CardDefaults.shape,
      onClick = onStartSet,
      modifier = Modifier
        .heightIn(max = 100.dp)
        .fillMaxSize()
    ) {
      Text(
        text = ifElse(
          condition = setState.setTimer != null,
          ifTrue = { stringResource(R.string.btn_start) },
          ifFalse = { stringResource(R.string.btn_done) }),
        style = MaterialTheme.typography.titleLarge
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerSheet(
  isVisible: Boolean,
  timerTitle: String,
  timerState: ExerciseScreenViewModel.TimerState?,
  sheetState: SheetState,
  onDismissRequest: () -> Unit,
  content: @Composable RowScope.() -> Unit,
) {
  if (isVisible) {
    val isActive = timerState?.isActive?.value == true
    val duration = timerState?.duration ?: 0.seconds

    val restStartTime = rememberSaveable(isActive) { System.currentTimeMillis() }
    var clockText by rememberSaveable { mutableStateOf(duration.toClockString()) }
    val progress = remember {
      if (duration.inWholeMilliseconds <= 0) Animatable(1f)
      else Animatable(initialValue = ((System.currentTimeMillis() - restStartTime).toFloat() / duration.inWholeMilliseconds.toFloat()))
    }

    LaunchedEffect(Unit) {
      val remainingMillis =
        duration.inWholeMilliseconds - (System.currentTimeMillis() - restStartTime)

      progress.animateTo(
        targetValue = 1f, animationSpec = tween(
          durationMillis = remainingMillis.toInt(), easing = LinearEasing
        )
      )
    }

    DisposableEffect(isActive) {
      val timer = if (isActive) timer(period = 1.seconds.inWholeMilliseconds) {
        val remainingMillis =
          duration.inWholeMilliseconds - (System.currentTimeMillis() - restStartTime)

        clockText = remainingMillis.milliseconds.toClockString()
      }
      else null

      onDispose {
        timer?.cancel()
        timer?.purge()
      }
    }

    ModalBottomSheet(
      sheetState = sheetState, onDismissRequest = onDismissRequest, dragHandle = null
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight(.8f)
          .pointerInput(Unit) {
            detectVerticalDragGestures(onVerticalDrag = { change, _ -> change.consume() })
          }) {
        Box(
          contentAlignment = Alignment.BottomCenter,
          modifier = Modifier
            .fillMaxWidth(.7f)
            .weight(3f)
        ) {
          Box(
            contentAlignment = Alignment.Center, modifier = Modifier.aspectRatio(1f)
          ) {
            CircularProgressIndicator(
              progress = { progress.value },
              strokeWidth = 20.dp,
              gapSize = 0.dp,
              strokeCap = StrokeCap.Butt,
              modifier = Modifier.fillMaxSize()
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(timerTitle, style = MaterialTheme.typography.titleMedium)
              Text(
                text = clockText,
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center
              )
              Spacer(Modifier.height(with(LocalDensity.current) {
                // Centers the text inside the progress indicator
                MaterialTheme.typography.titleMedium.lineHeight.toDp()
              }))
            }
          }
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
          modifier = Modifier
            .fillMaxWidth()
            .weight(2f)
            .padding(horizontal = 16.dp)
        ) {
          content()
        }
      }
    }
  }
}

@Composable
private fun InProgressDialog(
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) {
  AlertDialog(
    title = { Text(text = stringResource(R.string.dialog_title_exercise_in_progress)) },
    text = { Text(stringResource(R.string.dialog_text_exercise_in_progress)) },
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text(text = stringResource(R.string.btn_yes))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = stringResource(R.string.btn_cancel))
      }
    },
  )
}