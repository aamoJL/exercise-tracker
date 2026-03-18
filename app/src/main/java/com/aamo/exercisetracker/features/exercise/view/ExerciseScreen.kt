package com.aamo.exercisetracker.features.exercise.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.RoutineDatabase
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.features.exercise.view.components.InProgressDialog
import com.aamo.exercisetracker.features.exercise.view.components.RestTimerSheet
import com.aamo.exercisetracker.features.exercise.view.components.SetInfo
import com.aamo.exercisetracker.features.exercise.view.components.SetProgressIndicator
import com.aamo.exercisetracker.features.exercise.view.components.SetTimerSheet
import com.aamo.exercisetracker.features.exercise.view.use_cases.fetchExercise
import com.aamo.exercisetracker.features.exercise.view.use_cases.saveExerciseProgress
import com.aamo.exercisetracker.services.CountdownTimerService
import com.aamo.exercisetracker.services.ICountdownTimerService
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.components.inputs.BackNavigationIconButton
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.aamo.exercisetracker.utility.extensions.general.onNotNull
import com.aamo.exercisetracker.utility.extensions.general.onNull
import com.aamo.exercisetracker.utility.viewmodels.BasicTimer
import com.aamo.exercisetracker.utility.viewmodels.CountdownTimer
import com.aamo.exercisetracker.utility.viewmodels.ITimer
import com.aamo.exercisetracker.utility.viewmodels.ViewModelState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Date
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Serializable
data class ExerciseScreen(val id: Long = 0)

class ExerciseScreenViewModel(
  private val fetchData: suspend () -> ExerciseWithSets?,
  private val saveProgress: suspend (ExerciseProgress) -> Unit,
  timer: ITimer = BasicTimer()
) : ViewModel() {
  class TimerState(val duration: Duration) {
    val isActive = ViewModelState(false)
  }

  class UiState(exercise: Exercise, val sets: List<ExerciseSet>) {
    inner class SetState(val index: Int) {
      val set = sets.elementAtOrNull(index)
      val timerState = set?.let {
        if (it.valueType == ExerciseSet.ValueType.COUNTDOWN) TimerState(it.value.milliseconds)
        else null
      }
    }

    val exerciseName = exercise.name
    val currentSet = ViewModelState<SetState>(SetState(0))
    val restTimerState =
      if (exercise.restDuration > 0.seconds) TimerState(exercise.restDuration) else null
  }

  private val _countdownTimer: CountdownTimer = CountdownTimer(timer)

  var model: Exercise? = null
    private set

  val uiState = flow {
    emit(
      (fetchData() ?: throw IllegalStateException("Failed to fetch data")).let { (exercise, sets) ->
        model = exercise
        UiState(exercise = exercise, sets = sets)
      })
  }.catch { }.stateIn(
    scope = viewModelScope, started = SharingStarted.Lazily, initialValue = null
  )

  fun startSet(backgroundService: ICountdownTimerService? = null) {
    uiState.value?.also {
      it.currentSet.value.timerState.onNotNull { setTimer ->
        startCountdown(
          backgroundService = backgroundService,
          duration = setTimer.duration,
          onStart = { setTimer.isActive.update(true) },
          onFinished = { finishSet(backgroundService) },
          onCleanUp = { setTimer.isActive.update(false) })
      }.onNull {
        finishSet(backgroundService)
      }
    }
  }

  private fun finishSet(backgroundService: ICountdownTimerService?) {
    uiState.value?.apply {
      this.SetState(currentSet.value.index + 1).let { nextSet ->
        this.restTimerState.onNull {
          // no resting
          this.currentSet.update(nextSet)
        }.onNotNull { restTimer ->
          if (nextSet.set == null) this.currentSet.update(nextSet)
          else {
            // start resting
            restTimer.also { restTimer ->
              startCountdown(
                backgroundService = backgroundService,
                duration = restTimer.duration,
                onStart = { restTimer.isActive.update(true) },
                onFinished = { this.currentSet.update(nextSet) },
                onCleanUp = { restTimer.isActive.update(false) })
            }
          }
        }
      }
    }
  }

  fun finishExercise(finishedDate: Date) {
    viewModelScope.launch {
      runCatching {
        saveProgress(
          ExerciseProgress(exerciseId = checkNotNull(model).id, finishedDate = finishedDate)
        )
      }
    }
  }

  private fun startCountdown(
    backgroundService: ICountdownTimerService?,
    duration: Duration,
    onFinished: () -> Unit,
    onStart: () -> Unit,
    onCleanUp: () -> Unit,
  ) {
    if (duration <= 0.seconds) {
      if (_countdownTimer.isRunning) cancelCountdown()
      onStart(); onCleanUp(); onFinished()
      return
    }

    _countdownTimer.start(
      duration = duration,
      onStart = onStart,
      onFinished = onFinished,
      onCleanUp = onCleanUp,
      backgroundService = backgroundService
    )
  }

  fun stopCountdown() {
    _countdownTimer.stop()
  }

  fun cancelCountdown() {
    _countdownTimer.cancel()
  }
}

fun NavGraphBuilder.exerciseScreen(
  onBack: () -> Unit, onEdit: (exerciseId: Long, routineId: Long) -> Unit
) {
  composable<ExerciseScreen> { navStack ->
    val (exerciseId) = navStack.toRoute<ExerciseScreen>()
    val context = LocalContext.current
    val dao = RoutineDatabase.getDatabase(context.applicationContext).routineDao()
    val viewmodel: ExerciseScreenViewModel = viewModel(factory = viewModelFactory {
      initializer {
        ExerciseScreenViewModel(
          fetchData = { fetchExercise(dao = dao, exerciseId = exerciseId) },
          saveProgress = { progress ->
            saveExerciseProgress(dao = dao, progress = progress).also {
              onBack()
            }
          })
      }
    })
    val setTimerTitle = stringResource(R.string.title_set_timer)
    val restTimerTitle = stringResource(R.string.title_rest_timer)

    var timerService: CountdownTimerService? by remember { mutableStateOf(null) }
    val connection = remember {
      object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
          timerService = (binder as? CountdownTimerService.BinderHelper)?.getService()?.apply {
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

    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
      timerService?.hideNotification()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
      uiState?.apply {
        if (this.restTimerState?.isActive?.value == true) {
          timerService?.showNotification(restTimerTitle)
        }

        if (this.currentSet.value.timerState?.isActive?.value == true) {
          timerService?.showNotification(setTimerTitle)
        }
      }
    }

    DisposableEffect(Unit) {
      context.apply {
        bindService(
          Intent(this, CountdownTimerService::class.java), connection, Context.BIND_AUTO_CREATE
        )
      }

      onDispose { runCatching { context.unbindService(connection) } }
    }

    LoadingScreen(model = uiState) {
      ExerciseScreenContent(
        uiState = it,
        onBack = onBack,
        onEdit = { viewmodel.model?.also { model -> onEdit(exerciseId, model.routineId) } },
        onStartSet = { viewmodel.startSet(timerService) },
        onStopSetTimer = { viewmodel.stopCountdown() },
        onCancelSet = { viewmodel.cancelCountdown() },
        onStopRest = { viewmodel.stopCountdown() },
        onFinishExercise = { viewmodel.finishExercise(Date()) },
      )
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ExerciseScreenContent(
  uiState: ExerciseScreenViewModel.UiState,
  onBack: () -> Unit,
  onEdit: () -> Unit,
  onStartSet: () -> Unit,
  onStopSetTimer: () -> Unit,
  onCancelSet: () -> Unit,
  onStopRest: () -> Unit,
  onFinishExercise: () -> Unit,
) {
  val inProgress = uiState.currentSet.value.index != 0

  var openInProgressBackDialog by remember { mutableStateOf(false) }
  var openInProgressEditDialog by remember { mutableStateOf(false) }

  if (openInProgressBackDialog) {
    InProgressDialog(onDismiss = { openInProgressBackDialog = false }, onConfirm = {
      openInProgressBackDialog = false
      onBack()
    })
  }
  if (openInProgressEditDialog) {
    InProgressDialog(onDismiss = { openInProgressEditDialog = false }, onConfirm = {
      openInProgressEditDialog = false
      onEdit()
    })
  }

  BackHandler(enabled = inProgress) {
    openInProgressBackDialog = true
  }

  Scaffold(
    topBar = {
      TopAppBar(title = { Text(uiState.exerciseName) }, actions = {
        IconButton(onClick = onEdit) {
          Icon(
            painter = painterResource(R.drawable.rounded_edit_24),
            contentDescription = stringResource(R.string.cd_edit_exercise)
          )
        }
      }, navigationIcon = {
        BackNavigationIconButton(onBack = {
          if (inProgress) openInProgressBackDialog = true
          else onBack()
        })
      })
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
          currentSetNumber = uiState.currentSet.value.index + 1,
          totalSets = uiState.sets.size,
          modifier = Modifier.fillMaxWidth(.7f)
        )
        SetInfo(
          set = uiState.currentSet.value.set,
          onStartSet = onStartSet,
          onFinishExercise = onFinishExercise
        )
      }
    }
  }
  SetTimerSheet(
    show = uiState.currentSet.value.timerState?.isActive?.value == true,
    duration = uiState.currentSet.value.timerState?.duration ?: 0.seconds,
    onDismissRequest = onCancelSet,
    onStopSetTimer = onStopSetTimer,
    onCancelSet = onCancelSet,
  )
  RestTimerSheet(
    show = uiState.restTimerState?.isActive?.value == true,
    duration = uiState.restTimerState?.duration ?: 0.seconds,
    onDismissRequest = onStopRest,
    onStopRest = onStopRest
  )
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun Preview() {
  ExerciseTrackerTheme {
    ExerciseScreenContent(
      uiState = ExerciseScreenViewModel.UiState(
      exercise = Exercise(id = 1L, routineId = 1L, name = "Exercise 1", restDuration = 0.seconds),
      sets = listOf(
        ExerciseSet(
          id = 1L,
          exerciseId = 1L,
          value = 123,
          unit = "Reps",
          valueType = ExerciseSet.ValueType.REPETITION
        ), ExerciseSet(
          id = 2L,
          exerciseId = 1L,
          value = 123,
          unit = "Reps",
          valueType = ExerciseSet.ValueType.REPETITION
        ), ExerciseSet(
          id = 3L,
          exerciseId = 1L,
          value = 123,
          unit = "Reps",
          valueType = ExerciseSet.ValueType.REPETITION
        )
      )
    ).apply {
      this.currentSet.update(this.SetState(1))
    },
      onBack = {},
      onEdit = {},
      onStartSet = {},
      onStopSetTimer = {},
      onCancelSet = {},
      onStopRest = {},
      onFinishExercise = {})
  }
}