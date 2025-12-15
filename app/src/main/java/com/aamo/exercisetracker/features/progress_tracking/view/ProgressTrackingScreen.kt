package com.aamo.exercisetracker.features.progress_tracking.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.features.progress_tracking.records.components.DurationRecordDialog
import com.aamo.exercisetracker.features.progress_tracking.records.components.RepetitionRecordDialog
import com.aamo.exercisetracker.features.progress_tracking.view.components.CountdownSheet
import com.aamo.exercisetracker.features.progress_tracking.view.components.RecordChart
import com.aamo.exercisetracker.features.progress_tracking.view.components.StopwatchSheet
import com.aamo.exercisetracker.features.progress_tracking.view.models.ProgressTrackingTrackedProgressModel
import com.aamo.exercisetracker.features.progress_tracking.view.use_cases.fetchTrackedProgressFlow
import com.aamo.exercisetracker.features.progress_tracking.view.use_cases.saveTrackedProgressValue
import com.aamo.exercisetracker.services.CountdownTimerService
import com.aamo.exercisetracker.services.ICountdownTimerService
import com.aamo.exercisetracker.services.IStopwatchTimerService
import com.aamo.exercisetracker.services.StopwatchTimerService
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.components.inputs.BackNavigationIconButton
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.aamo.exercisetracker.utility.viewmodels.ViewModelState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Serializable
data class ProgressTrackingScreen(val progressId: Long)

class ProgressTrackingScreenViewModel(
  fetchData: () -> Flow<ProgressTrackingTrackedProgressModel>,
  private val addValue: suspend (TrackedProgressValue) -> Unit,
) : ViewModel() {
  class CountdownTimerState {
    var duration by mutableStateOf(0.seconds)
    val isActive = ViewModelState(false).transformation { value -> value && duration > 0.seconds }
  }

  class StopwatchTimerState {
    val isActive = ViewModelState(false)
    val finalDuration = ViewModelState(0.milliseconds)
  }

  val model = fetchData().catch { }.onEach {
    it.countdownTime?.also { duration -> countdownTimerState.duration = duration }
  }.stateIn(
    scope = viewModelScope, started = SharingStarted.Lazily, initialValue = null
  )

  val countdownTimerState = CountdownTimerState()
  val stopwatchTimerState = StopwatchTimerState()

  fun addNewValue(value: Int, date: Date) {
    viewModelScope.launch {
      runCatching {
        checkNotNull(model.value).also { model ->
          addValue(TrackedProgressValue(progressId = model.id, value = value, addedDate = date))
        }
      }
    }
  }

  fun startCountdown(countdownTimerService: ICountdownTimerService) {
    countdownTimerService.cancel()

    countdownTimerState.also { timerState ->
      val duration = timerState.duration

      if (duration <= 0.seconds) return

      countdownTimerService.start(
        durationMillis = duration.inWholeMilliseconds,
        onStart = { timerState.isActive.update(true) },
        onFinished = { /* Nothing here */ },
        onCleanUp = { timerState.isActive.update(false) })
    }
  }

  fun cancelCountdown(countdownTimerService: ICountdownTimerService) {
    countdownTimerService.cancel()
  }

  fun startStopwatch(stopwatchTimerService: IStopwatchTimerService) {
    stopwatchTimerService.cancel()
    stopwatchTimerService.start(
      onStart = { stopwatchTimerState.isActive.update(true) },
      onFinished = { stopwatchTimerState.finalDuration.update(it) },
      onCleanUp = { stopwatchTimerState.isActive.update(false) })
  }

  fun stopStopwatch(stopwatchTimerService: IStopwatchTimerService) {
    stopwatchTimerService.stop()
  }

  fun cancelStopwatch(stopwatchTimerService: IStopwatchTimerService) {
    stopwatchTimerService.cancel()
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun NavGraphBuilder.progressTrackingScreen(
  onBack: () -> Unit, onEdit: (id: Long) -> Unit, onShowRecords: (id: Long) -> Unit
) {
  composable<ProgressTrackingScreen> { navStack ->
    val context = LocalContext.current
    val progressId = navStack.toRoute<ProgressTrackingScreen>().progressId
    val dao = RoutineDatabase.getDatabase(context.applicationContext).trackedProgressDao()
    val viewmodel: ProgressTrackingScreenViewModel = viewModel(factory = viewModelFactory {
      initializer {
        ProgressTrackingScreenViewModel(
          fetchData = { fetchTrackedProgressFlow(dao = dao, progressId = progressId) },
          addValue = { value -> saveTrackedProgressValue(dao = dao, value = value) },
        )
      }
    })
    val timerTitle = stringResource(R.string.title_timer)
    val stopwatchTitle = stringResource(R.string.title_stopwatch)

    var countDownService: CountdownTimerService? by remember { mutableStateOf(null) }
    var stopwatchService: StopwatchTimerService? by remember { mutableStateOf(null) }
    val timerConnection = remember {
      object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
          // Notifications needs to be hidden here, because the service will be null in
          //  LifecycleEventEffect ON_RESUME when the configuration changes
          when (binder) {
            is CountdownTimerService.BinderHelper -> binder.getService().apply {
              countDownService = this
              hideNotification()
            }

            is StopwatchTimerService.BinderHelper -> binder.getService().apply {
              stopwatchService = this
              hideNotification()
            }
          }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
          countDownService = null
          stopwatchService = null
        }
      }
    }

    val model by viewmodel.model.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
      countDownService?.hideNotification()
      stopwatchService?.hideNotification()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
      if (viewmodel.countdownTimerState.isActive.value) {
        countDownService?.showNotification(timerTitle)
      }
      if (viewmodel.stopwatchTimerState.isActive.value) {
        stopwatchService?.showNotification(stopwatchTitle)
      }
    }

    DisposableEffect(model?.progressType) {
      context.apply {
        when (model?.progressType) {
          ProgressTrackingTrackedProgressModel.ProgressType.COUNTDOWN -> CountdownTimerService::class.java
          ProgressTrackingTrackedProgressModel.ProgressType.STOPWATCH -> StopwatchTimerService::class.java
          else -> null
        }?.also {
          bindService(Intent(this, it), timerConnection, Context.BIND_AUTO_CREATE)
        }
      }

      onDispose {
        runCatching { context.unbindService(timerConnection) }
      }
    }

    LoadingScreen(loading = model == null) {
      ProgressTrackingScreenContent(
        model = checkNotNull(model),
        stopwatchTimerState = viewmodel.stopwatchTimerState,
        countdownTimerState = viewmodel.countdownTimerState,
        onEdit = { onEdit(progressId) },
        onBack = onBack,
        onShowRecords = { onShowRecords(progressId) },
        onAddValue = { value, date -> viewmodel.addNewValue(value, date) },
        onStartCountdown = { countDownService?.also { viewmodel.startCountdown(it) } },
        onCancelCountdown = { countDownService?.also { viewmodel.cancelCountdown(it) } },
        onStartStopwatch = { stopwatchService?.also { viewmodel.startStopwatch(it) } },
        onStopStopwatch = { stopwatchService?.also { viewmodel.stopStopwatch(it) } },
        onCancelStopwatch = { stopwatchService?.also { viewmodel.cancelStopwatch(it) } },
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressTrackingScreenContent(
  model: ProgressTrackingTrackedProgressModel,
  stopwatchTimerState: ProgressTrackingScreenViewModel.StopwatchTimerState,
  countdownTimerState: ProgressTrackingScreenViewModel.CountdownTimerState,
  onEdit: () -> Unit,
  onBack: () -> Unit,
  onShowRecords: () -> Unit,
  onAddValue: (value: Int, date: Date) -> Unit,
  onStartCountdown: () -> Unit,
  onCancelCountdown: () -> Unit,
  onStartStopwatch: () -> Unit,
  onStopStopwatch: () -> Unit,
  onCancelStopwatch: () -> Unit,
) {
  var showNewRecordDialog by remember { mutableStateOf(false) }
  var showTimerModal by remember { mutableStateOf(false) }

  if (showNewRecordDialog) {
    when (model.progressType) {
      ProgressTrackingTrackedProgressModel.ProgressType.STOPWATCH -> DurationRecordDialog(
        label = stringResource(R.string.dialog_title_add_new_record),
        initDuration = stopwatchTimerState.finalDuration.value,
        date = Date(),
        onConfirm = { duration, date ->
          onAddValue(duration.inWholeMilliseconds.toInt(), date)
          showNewRecordDialog = false
          stopwatchTimerState.finalDuration.update(0.seconds)
        },
        onDismiss = {
          showNewRecordDialog = false
          stopwatchTimerState.finalDuration.update(0.seconds)
        })

      else -> RepetitionRecordDialog(
        label = stringResource(R.string.dialog_title_add_new_record),
        value = 0,
        valueUnit = model.recordUnit,
        date = Date(),
        onConfirm = { value, date ->
          onAddValue(value, date)
          showNewRecordDialog = false
        },
        onDismiss = { showNewRecordDialog = false },
      )
    }
  }

  Scaffold(topBar = {
    TopAppBar(title = { Text(model.name) }, actions = {
      IconButton(onClick = onEdit) {
        Icon(
          painter = painterResource(R.drawable.rounded_edit_24),
          contentDescription = stringResource(R.string.cd_edit_tracked_progress)
        )
      }
      IconButton(enabled = model.values.isNotEmpty(), onClick = onShowRecords) {
        Icon(
          painter = painterResource(R.drawable.rounded_list_24),
          contentDescription = stringResource(R.string.cd_show_records)
        )
      }
    }, navigationIcon = { BackNavigationIconButton(onBack = onBack) })
  }, floatingActionButton = {
    Column {
      if (model.progressType == ProgressTrackingTrackedProgressModel.ProgressType.COUNTDOWN || model.progressType == ProgressTrackingTrackedProgressModel.ProgressType.STOPWATCH) {
        FloatingActionButton(
          shape = CircleShape,
          containerColor = ButtonDefaults.buttonColors().containerColor,
          onClick = { showTimerModal = true },
          modifier = Modifier.padding(8.dp)
        ) {
          Icon(
            painter = painterResource(R.drawable.rounded_timer_24),
            contentDescription = stringResource(R.string.cd_timer)
          )
        }
      }
      FloatingActionButton(
        shape = CircleShape,
        containerColor = ButtonDefaults.buttonColors().containerColor,
        onClick = { showNewRecordDialog = true },
        modifier = Modifier.padding(8.dp)
      ) {
        Icon(
          painter = painterResource(R.drawable.rounded_add_24),
          contentDescription = stringResource(R.string.cd_add)
        )
      }
    }
  }) { innerPadding ->
    Surface(
      modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize()
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier) {
        RecordChart(
          model = model,
          modifier = Modifier
            .fillMaxWidth()
            .sizeIn(maxHeight = 500.dp)
            .padding(16.dp)
        )
      }
    }
  }
  when (model.progressType) {
    ProgressTrackingTrackedProgressModel.ProgressType.COUNTDOWN -> CountdownSheet(
      show = showTimerModal,
      active = countdownTimerState.isActive.value,
      duration = countdownTimerState.duration,
      title = stringResource(R.string.title_timer),
      onDismissRequest = {
        showTimerModal = false
        onCancelCountdown()
      },
      onStart = onStartCountdown,
      onCancel = {
        showTimerModal = false
        onCancelCountdown()
      })

    ProgressTrackingTrackedProgressModel.ProgressType.STOPWATCH -> StopwatchSheet(
      show = showTimerModal,
      active = stopwatchTimerState.isActive.value,
      title = stringResource(R.string.title_stopwatch),
      onDismissRequest = {
        showTimerModal = false
        onCancelStopwatch()
      },
      onStart = onStartStopwatch,
      onStop = {
        showTimerModal = false
        onStopStopwatch()
        showNewRecordDialog = true
      },
      onCancel = {
        showTimerModal = false
        onCancelStopwatch()
      },
    )

    else -> {}
  }
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun Preview() {
  ExerciseTrackerTheme {
    ProgressTrackingScreenContent(
      model = ProgressTrackingTrackedProgressModel(
        id = 1L,
        name = "Progress 1",
        progressType = ProgressTrackingTrackedProgressModel.ProgressType.REPETITION,
        values = listOf(1, 2, 3),
        recordUnit = "Reps",
        countdownTime = null
      ),
      stopwatchTimerState = ProgressTrackingScreenViewModel.StopwatchTimerState(),
      countdownTimerState = ProgressTrackingScreenViewModel.CountdownTimerState(),
      onEdit = {},
      onBack = {},
      onShowRecords = {},
      onAddValue = { _, _ -> },
      onStartCountdown = {},
      onCancelCountdown = {},
      onStartStopwatch = {},
      onStopStopwatch = {},
      onCancelStopwatch = {})
  }
}