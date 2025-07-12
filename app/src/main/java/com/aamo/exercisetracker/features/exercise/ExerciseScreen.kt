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
import com.aamo.exercisetracker.database.RoutineDatabase
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithProgressAndSets
import com.aamo.exercisetracker.database.entities.RoutineDao
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

class ExerciseScreenViewModel(private val exerciseId: Long, private val routineDao: RoutineDao) :
        ViewModel() {
  data class SetState(
    val sets: List<ExerciseSet> = emptyList(),
    val index: Int = 0,
    private val timerActive: Boolean = false,
  ) {
    val set: ExerciseSet? = sets.elementAtOrNull(index)
    val total: Int = sets.size
    val timer: TimerState = initTimer()

    private fun initTimer(): TimerState {
      val hasTimer = set?.valueType?.equals(ExerciseSet.ValueType.COUNTDOWN) == true

      return TimerState(
        isActive = hasTimer && timerActive,
        duration = if (hasTimer) set.value.milliseconds else 0.milliseconds
      )
    }
  }

  data class TimerState(
    val isActive: Boolean = false,
    val duration: Duration = 0.milliseconds,
  )

  var exerciseModel: ExerciseWithProgressAndSets? by mutableStateOf(null)
    private set
  var setState by mutableStateOf(SetState())
    private set
  var restState by mutableStateOf(TimerState())
    private set
  var isLoading by mutableStateOf(true)
    private set
  var inProgress by mutableStateOf(false)
    private set

  init {
    viewModelScope.launch {
      routineDao.getExerciseWithProgressAndSets(exerciseId).let {
        exerciseModel = it
        setState = SetState(sets = it?.sets ?: emptyList())
        restState = TimerState(duration = it?.exercise?.restDuration ?: 0.seconds)
        isLoading = false
      }
    }
  }

  fun startSet(countDownTimerService: CountDownTimerService) {
    inProgress = true

    if (setState.timer.duration > 0.milliseconds) {
      startTimer(
        countDownTimerService = countDownTimerService,
        title = "Set timer",
        duration = setState.timer.duration,
        onStart = { setState = setState.copy(timerActive = true) },
        onFinished = {
          setState = setState.copy(timerActive = false)
          finishSet(countDownTimerService)
        })
    }
    else {
      finishSet(countDownTimerService)
    }
  }

  fun finishExercise(onSaved: () -> Unit) {
    inProgress = false

    // Save progress
    viewModelScope.launch {
      exerciseModel?.let { model ->
        val finishedDate = Calendar.getInstance().time
        val progress = model.progress?.copy(finishedDate = finishedDate) ?: ExerciseProgress(
          exerciseId = model.exercise.id, finishedDate = finishedDate
        )

        routineDao.upsert(progress).also {
          onSaved()
        }
      }
    }
  }

  fun stopTimer(countDownTimerService: CountDownTimerService) {
    countDownTimerService.stop()
  }

  fun cancelSetTimer(countDownTimerService: CountDownTimerService) {
    countDownTimerService.cancel()
    setState = setState.copy(timerActive = false)
  }

  private fun finishSet(countDownTimerService: CountDownTimerService) {
    setState.copy(index = setState.index + 1, timerActive = false).let { nextSetState ->
      nextSetState.set.onNull {
        // Was final set, no resting
        setState = nextSetState
      }.onNotNull {
        startTimer(
          countDownTimerService = countDownTimerService,
          title = "Rest timer",
          duration = restState.duration,
          onStart = { restState = restState.copy(isActive = true) },
          onFinished = {
            restState = restState.copy(isActive = false)
            setState = nextSetState
          })
      }
    }
  }

  private fun startTimer(
    countDownTimerService: CountDownTimerService,
    title: String,
    duration: Duration,
    onStart: () -> Unit,
    onFinished: () -> Unit
  ) {
    if (duration <= 0.seconds) {
      onFinished()
      return
    }

    countDownTimerService.start(
      title = title, durationMillis = duration.inWholeMilliseconds, onFinished = onFinished
    ).also {
      onStart()
    }
  }
}

fun NavGraphBuilder.exerciseScreen(onBack: () -> Unit, onEdit: (id: Long) -> Unit) {
  composable<ExerciseScreen> { navStack ->
    val (id) = navStack.toRoute<ExerciseScreen>()
    val context = LocalContext.current.applicationContext
    val viewmodel: ExerciseScreenViewModel = viewModel(factory = viewModelFactory {
      initializer {
        ExerciseScreenViewModel(
          exerciseId = id, routineDao = RoutineDatabase.getDatabase(context).routineDao(),
        )
      }
    })
    val exercise = viewmodel.exerciseModel?.exercise
    val setState = viewmodel.setState
    val restState = viewmodel.restState

    var service: CountDownTimerService? by remember { mutableStateOf(null) }
    val connection = remember {
      object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
          service = (binder as? CountDownTimerService.BinderHelper)?.getService()?.apply {
            // Notifications needs to be hidden also here, because the service will be null in
            //  LifecycleEventEffect ON_RESUME when the configuration changes
            hideNotification()
          }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
          service = null
        }
      }
    }

    var openInProgressBackDialog by remember { mutableStateOf(false) }
    var openInProgressEditDialog by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
      service?.hideNotification()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
      if (viewmodel.restState.isActive || viewmodel.setState.timer.isActive) {
        service?.showNotification()
      }
    }

    BackHandler(enabled = viewmodel.inProgress) {
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
        onEdit(id)
      })
    }

    DisposableEffect(Unit) {
      context.bindService(
        Intent(context, CountDownTimerService::class.java), connection, Context.BIND_AUTO_CREATE
      ).onNull {
        Log.e("asd", "Did not bind")
      }

      onDispose {
        context.unbindService(connection)
      }
    }

    LoadingScreen(enabled = viewmodel.isLoading) {
      if (exercise != null) {
        ExerciseScreen(
          exercise = exercise,
          setState = setState,
          restState = restState,
          onBack = {
            viewmodel.inProgress.onTrue { openInProgressBackDialog = true }.onFalse { onBack() }
          },
          onEdit = {
            viewmodel.inProgress.onTrue { openInProgressEditDialog = true }.onFalse { onEdit(id) }
          },
          onStartSet = { service?.let { viewmodel.startSet(it) } },
          onStopSetTimer = { service?.let { viewmodel.stopTimer(it) } },
          onCancelSet = { service?.let { viewmodel.cancelSetTimer(it) } },
          onStopRest = { service?.let { viewmodel.stopTimer(it) } },
          onFinishExercise = { viewmodel.finishExercise(onSaved = { onBack() }) },
        )
      }
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ExerciseScreen(
  exercise: Exercise,
  setState: ExerciseScreenViewModel.SetState,
  restState: ExerciseScreenViewModel.TimerState,
  onBack: () -> Unit,
  onEdit: () -> Unit,
  onStartSet: () -> Unit,
  onStopSetTimer: () -> Unit,
  onCancelSet: () -> Unit,
  onStopRest: () -> Unit,
  onFinishExercise: () -> Unit,
) {
  val restSheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true,
    confirmValueChange = { false /* Prevents closing by pressing outside the sheet */ })
  val setTimerSheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true,
    confirmValueChange = { false /* Prevents closing by pressing outside the sheet */ })
  var showRestSheet by remember { mutableStateOf(false) }
  var showSetTimerSheet by remember { mutableStateOf(false) }

  LaunchedEffect(restState.isActive) {
    restSheetState.apply {
      if (restState.isActive) show() else hide()
    }
    // sheet state can't be checked with SheetState.currentValue, because confirmValueChange
    //  is always false, so the state will not change when closing the sheet.
    showRestSheet = restState.isActive
  }

  LaunchedEffect(setState.timer.isActive) {
    setTimerSheetState.apply {
      if (setState.timer.isActive) show() else hide()
    }
    // sheet state can't be checked with SheetState.currentValue, because confirmValueChange
    //  is always false, so the state will not change when closing the sheet.
    showSetTimerSheet = setState.timer.isActive
  }

  Scaffold(
    topBar = {
      TopAppBar(title = { Text(exercise.name) }, actions = {
        IconButton(onClick = onEdit) {
          Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit exercise")
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
        SetProgress(currentSet = setState.index + 1, totalSets = setState.total)

        if (setState.set != null) {
          SetContent(setState = setState, onSubmit = {
            onStartSet()
          })
        }
        else {
          Button(
            shape = CardDefaults.shape,
            onClick = onFinishExercise,
            modifier = Modifier
              .heightIn(max = 100.dp)
              .fillMaxSize()
          ) {
            Text("Finish", style = MaterialTheme.typography.titleLarge)
          }
        }
      }
    }

    // Visibility needs to be checked with showRestSheet instead of setState.isResting because
    //  otherwise the sheet closing animation will not work correctly.
    TimerSheet(
      isVisible = showRestSheet,
      timerTitle = "Rest",
      timerState = restState,
      sheetState = restSheetState,
      onDismissRequest = onStopRest,
    ) {
      Button(
        onClick = onStopRest, colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.secondary
        )
      ) {
        Text("Stop")
      }
    }
    TimerSheet(
      isVisible = showSetTimerSheet,
      timerTitle = "Set timer",
      timerState = setState.timer,
      sheetState = setTimerSheetState,
      onDismissRequest = onCancelSet,
    ) {
      Button(
        onClick = onCancelSet, colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error
        ), modifier = Modifier.weight(1f)
      ) {
        Text("Cancel")
      }
      Button(
        onClick = onStopSetTimer, colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.secondary
        ), modifier = Modifier.weight(1f)
      ) {
        Text("Finish")
      }
    }
  }
}

@Composable
fun SetProgress(currentSet: Int, totalSets: Int) {
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
        text = ifElse(currentSet > totalSets, "Completed", "Set"),
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
fun SetContent(setState: ExerciseScreenViewModel.SetState, onSubmit: () -> Unit) {
  fun getSetValueString(set: ExerciseSet?): String {
    return when (set?.valueType) {
      ExerciseSet.ValueType.COUNTDOWN -> set.value.milliseconds.inWholeMinutes.toString()
      null -> String.EMPTY
      else -> set.value.toString()
    }
  }

  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Card {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .padding(24.dp)
          .fillMaxWidth()
      ) {
        Text("Current set", style = MaterialTheme.typography.titleMedium)
        Text(
          text = "${getSetValueString(setState.set)} ${setState.set?.unit}",
          style = MaterialTheme.typography.displayMedium
        )
      }
    }
    Button(
      shape = CardDefaults.shape,
      onClick = onSubmit,
      modifier = Modifier
        .heightIn(max = 100.dp)
        .fillMaxSize()
    ) {
      Text(
        text = ifElse(setState.timer.duration > 0.milliseconds, "Start", "Done"),
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
  timerState: ExerciseScreenViewModel.TimerState,
  sheetState: SheetState,
  onDismissRequest: () -> Unit,
  content: @Composable RowScope.() -> Unit,
) {
  if (isVisible) {
    val restStartTime = rememberSaveable(timerState.isActive) { System.currentTimeMillis() }
    var clockText by rememberSaveable { mutableStateOf(timerState.duration.toClockString()) }
    val progress = remember {
      if (timerState.duration.inWholeMilliseconds <= 0) Animatable(1f)
      else Animatable(initialValue = ((System.currentTimeMillis() - restStartTime).toFloat() / timerState.duration.inWholeMilliseconds.toFloat()))
    }

    LaunchedEffect(Unit) {
      val remainingMillis =
        timerState.duration.inWholeMilliseconds - (System.currentTimeMillis() - restStartTime)

      progress.animateTo(
        targetValue = 1f, animationSpec = tween(
          durationMillis = remainingMillis.toInt(), easing = LinearEasing
        )
      )
    }

    DisposableEffect(timerState.isActive) {
      val timer = if (timerState.isActive) timer(period = 1.seconds.inWholeMilliseconds) {
        val remainingMillis =
          timerState.duration.inWholeMilliseconds - (System.currentTimeMillis() - restStartTime)

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
    title = { Text(text = "Exercise in progress") },
    text = { Text("Do you want to stop the exercise?") },
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text(text = "Yes")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = "Cancel")
      }
    },
  )
}