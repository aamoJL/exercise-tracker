package com.aamo.exercisetracker.features.exercise

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.lifecycle.ViewModel
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
import com.aamo.exercisetracker.utility.extensions.date.toClockString
import com.aamo.exercisetracker.utility.extensions.general.applyIf
import com.aamo.exercisetracker.utility.extensions.general.onFalse
import com.aamo.exercisetracker.utility.extensions.general.onNotNull
import com.aamo.exercisetracker.utility.extensions.general.onNull
import com.aamo.exercisetracker.utility.extensions.general.onTrue
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Calendar
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Serializable
data class ExerciseScreen(val id: Long = 0)

class ExerciseScreenViewModel(private val exerciseId: Long, private val routineDao: RoutineDao) :
        ViewModel() {
  data class SetState(
    val set: ExerciseSet? = null,
    val index: Int = 0,
    val total: Int = 0,
  )

  data class RestState(
    val isResting: Boolean = false,
    val restDuration: Duration = 0.milliseconds,
  )

  var exerciseModel: ExerciseWithProgressAndSets? by mutableStateOf(null)
    private set
  var setState by mutableStateOf(SetState())
    private set
  var restState by mutableStateOf(RestState())
    private set
  var isLoading by mutableStateOf(true)
    private set
  var inProgress by mutableStateOf(false)
    private set

  init {
    viewModelScope.launch {
      routineDao.getExerciseWithProgressAndSets(exerciseId).let {
        exerciseModel = it
        setState = SetState(
          set = it?.sets?.firstOrNull(), index = 0, total = it?.sets?.size ?: 0
        )
        restState = RestState(restDuration = it?.exercise?.restDuration ?: 0.seconds)
        isLoading = false
      }
    }
  }

  fun setCompleted(countDownTimerService: CountDownTimerService) {
    getNextSetState().let { nextSetState ->
      nextSetState.set.onNull {
        setState = nextSetState
        inProgress = false

        viewModelScope.launch {
          exerciseModel?.let { model ->
            val finishedDate = Calendar.getInstance().time
            val progress = model.progress?.copy(finishedDate = finishedDate) ?: ExerciseProgress(
              exerciseId = model.exercise.id, finishedDate = finishedDate
            )

            routineDao.upsert(progress)
          }
        }
      }.onNotNull {
        inProgress = true
        countDownTimerService.start(
          durationMillis = restState.restDuration.inWholeMilliseconds, onFinished = {
            stopRest(countDownTimerService)
          })
        restState = restState.copy(isResting = true)
      }
    }
  }

  fun stopRest(countDownTimerService: CountDownTimerService) {
    countDownTimerService.stop()

    setState = getNextSetState()
    restState = restState.copy(isResting = false)
  }

  private fun getNextSetState(): SetState {
    val nextIndex = Math.clamp((setState.index + 1).toLong(), 0, setState.total)
    val nextSet = exerciseModel?.sets?.elementAtOrNull(nextIndex)

    return setState.copy(set = nextSet, index = nextIndex)
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
          service = (binder as? CountDownTimerService.BinderHelper)?.getService()
            ?.apply { title = "Rest Timer" }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
          service = null
        }
      }
    }

    var openInProgressBackDialog by remember { mutableStateOf(false) }
    var openInProgressEditDialog by remember { mutableStateOf(false) }

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
          onSetCompleted = { service?.let { viewmodel.setCompleted(it) } },
          onStopRest = { service?.let { viewmodel.stopRest(it) } })
      }
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ExerciseScreen(
  exercise: Exercise,
  setState: ExerciseScreenViewModel.SetState,
  restState: ExerciseScreenViewModel.RestState,
  onBack: () -> Unit,
  onEdit: () -> Unit,
  onSetCompleted: () -> Unit,
  onStopRest: () -> Unit,
) {
  val restSheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true,
    confirmValueChange = { false /* Prevents closing by pressing outside the sheet */ })
  val set = remember(setState.set) { setState.set }
  var showRestSheet by remember { mutableStateOf(false) }

  LaunchedEffect(restState.isResting) {
    restSheetState.apply {
      if (restState.isResting) show() else hide()
    }

    // sheet state can't be checked with SheetState.currentValue, because confirmValueChange
    //  is always false, so the state will not change when closing the sheet.
    showRestSheet = restState.isResting
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
        SetProgress(current = setState.index + 1, total = setState.total)
        if (set != null) {
          SetContent(set = set, isResting = restState.isResting, onDone = onSetCompleted)
        }
      }
    }

    // Visibility needs to be checked with showRestSheet instead of setState.isResting because the
    //  sheet closing animation will not work correctly.
    RestSheet(
      isVisible = showRestSheet,
      restState = restState,
      sheetState = restSheetState,
      onDismissRequest = onStopRest,
    )
  }
}

@Composable
fun SetProgress(current: Int, total: Int) {
  val percent = (current.toFloat() - 1) / total.toFloat()

  Box(
    contentAlignment = Alignment.Center, modifier = Modifier
      .fillMaxWidth(.7f)
      .aspectRatio(1f)
  ) {
    /* TODO: Segmented progress indicator */
    CircularProgressIndicator(
      progress = { percent },
      strokeWidth = 20.dp,
      gapSize = 0.dp,
      strokeCap = StrokeCap.Butt,
      modifier = Modifier.fillMaxSize()
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      if (current > total) {
        Text("Completed", style = MaterialTheme.typography.titleMedium)
      }
      else {
        Text("Set", style = MaterialTheme.typography.titleMedium)
      }
      Text(
        text = "${min(current, total)}/$total",
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
fun SetContent(set: ExerciseSet, isResting: Boolean, onDone: () -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Card {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .padding(24.dp)
          .fillMaxWidth()
      ) {
        Text("Current set", style = MaterialTheme.typography.titleMedium)
        Text("${set.value} ${set.unit}", style = MaterialTheme.typography.displayMedium)
      }
    }
    Button(
      enabled = !isResting,
      shape = CardDefaults.shape,
      onClick = onDone,
      modifier = Modifier
        .heightIn(max = 100.dp)
        .fillMaxSize()
    ) {
      Text("Done", style = MaterialTheme.typography.titleLarge)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestSheet(
  isVisible: Boolean,
  restState: ExerciseScreenViewModel.RestState,
  sheetState: SheetState,
  onDismissRequest: () -> Unit
) {
  if (isVisible) {
    var remainingMillis by rememberSaveable(restState.restDuration) { mutableLongStateOf(restState.restDuration.inWholeMilliseconds) }
    val progress = remember {
      if (restState.restDuration.inWholeMilliseconds <= 0) Animatable(1f)
      else Animatable(1f - (remainingMillis.toFloat() / restState.restDuration.inWholeMilliseconds.toFloat()))
    }

    LaunchedEffect(Unit) {
      progress.animateTo(
        targetValue = 1f, animationSpec = tween(
          durationMillis = remainingMillis.toInt(), easing = LinearEasing
        )
      )
    }

    DisposableEffect(restState.isResting) {
      var timer: CountDownTimer? = applyIf(
        condition = restState.isResting,
        value = object : CountDownTimer(remainingMillis, 1.seconds.inWholeMilliseconds) {
          override fun onTick(remaining: Long) {
            remainingMillis = remaining
          }

          override fun onFinish() {
            cancel()
          }
        }.start()
      )

      onDispose {
        timer?.cancel()
      }
    }

    ModalBottomSheet(
      sheetState = sheetState, onDismissRequest = onDismissRequest, dragHandle = null
    ) {
      Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight(.8f)
          .pointerInput(Unit) {
            detectVerticalDragGestures(onVerticalDrag = { change, _ -> change.consume() })
            detectTapGestures(onPress = {})
          }) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .fillMaxHeight(.8f)
            .fillMaxWidth(.7f)
            .aspectRatio(1f)
        ) {
          CircularProgressIndicator(
            progress = { progress.value },
            strokeWidth = 20.dp,
            gapSize = 0.dp,
            strokeCap = StrokeCap.Butt,
            modifier = Modifier.fillMaxSize()
          )
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Rest", style = MaterialTheme.typography.titleMedium)
            Text(
              text = remainingMillis.milliseconds.toClockString(),
              style = MaterialTheme.typography.displayLarge,
              textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(with(LocalDensity.current) {
              // Centers the text inside the progress indicator
              MaterialTheme.typography.titleMedium.lineHeight.toDp()
            }))
          }
        }
        Button(onClick = onDismissRequest) {
          Text("Stop")
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