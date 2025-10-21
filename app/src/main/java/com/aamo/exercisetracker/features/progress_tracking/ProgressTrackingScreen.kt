package com.aamo.exercisetracker.features.progress_tracking

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.icu.text.DecimalFormat
import android.os.IBinder
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
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
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.features.progress_tracking.ProgressTrackingScreenViewModel.StopwatchTimerState
import com.aamo.exercisetracker.features.progress_tracking.use_cases.fetchTrackedProgressFlow
import com.aamo.exercisetracker.features.progress_tracking.use_cases.saveTrackedProgressValue
import com.aamo.exercisetracker.services.CountDownTimerService
import com.aamo.exercisetracker.services.StopwatchTimerService
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.GesturelessModalBottomSheet
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.utility.extensions.date.toClockString
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.extensions.general.trimFirst
import com.aamo.exercisetracker.utility.viewmodels.ViewModelState
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.extensions.format
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Date
import kotlin.concurrent.timer
import kotlin.math.ceil
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Serializable
data class ProgressTrackingScreen(val progressId: Long)

class ProgressTrackingScreenViewModel(
  private val fetchData: () -> Flow<Model>,
  private val addValue: suspend (value: Int, date: Date) -> Unit,
) : ViewModel() {
  data class Model(
    val progressName: String,
    val recordValueUnit: String,
    val records: List<Int>,
    val recordType: RecordType,
    val countDownTime: Duration?
  ) {
    enum class RecordType {
      REPETITION,
      TIMER,
      STOPWATCH
    }
  }

  class CountDownTimerState(val duration: Duration) {
    val isActive =
      ViewModelState(false).validation { value -> value && duration > 0.seconds }.onChange {
        if (it) isFinished = false
      }
    var isFinished by mutableStateOf(false)
  }

  class StopwatchTimerState {
    val isActive = ViewModelState(false)
    val finalDuration = ViewModelState(0.milliseconds)
  }

  class UiState {
    var progressName by mutableStateOf(String.EMPTY)
    var recordValueUnit by mutableStateOf(String.EMPTY)
    var records by mutableStateOf(emptyList<Int>())
    var recordType by mutableStateOf(Model.RecordType.REPETITION)
    var countDownTimerState: CountDownTimerState = CountDownTimerState(0.milliseconds)
    var stopwatchTimerState: StopwatchTimerState = StopwatchTimerState()
    var isLoading by mutableStateOf(true)
  }

  val uiState = UiState()

  init {
    viewModelScope.launch {
      runCatching {
        fetchData().collect { result ->
          uiState.apply {
            progressName = result.progressName
            recordValueUnit = result.recordValueUnit
            recordType = result.recordType
            result.countDownTime?.also {
              countDownTimerState = CountDownTimerState(duration = result.countDownTime)
            }
            records = result.records
            isLoading = false
          }
        }
      }.onFailure { }
    }
  }

  fun addNewValue(value: Int, date: Date) {
    viewModelScope.launch {
      addValue(value, date)
    }
  }

  fun startTimer(countDownTimerService: CountDownTimerService) {
    countDownTimerService.cancel()

    uiState.countDownTimerState.also { timerState ->
      val duration = timerState.duration

      if (duration <= 0.seconds) {
        return
      }

      countDownTimerService.start(
        durationMillis = duration.inWholeMilliseconds,
        onStart = { timerState.isActive.update(true) },
        onFinished = { timerState.isFinished = true },
        onCleanUp = { timerState.isActive.update(false) })
    }
  }

  fun startTimer(stopwatchTimerService: StopwatchTimerService) {
    stopwatchTimerService.apply {
      cancel()
      start(
        onStart = { uiState.stopwatchTimerState.isActive.update(true) },
        onFinished = { uiState.stopwatchTimerState.finalDuration.update(it) },
        onCleanUp = { uiState.stopwatchTimerState.isActive.update(false) })
    }
  }

  fun stopTimer(stopwatchTimerService: StopwatchTimerService) {
    stopwatchTimerService.stop()
  }

  fun cancelTimer(countDownTimerService: CountDownTimerService) {
    countDownTimerService.cancel()
  }

  fun cancelTimer(stopwatchTimerService: StopwatchTimerService) {
    stopwatchTimerService.cancel()
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun NavGraphBuilder.progressTrackingScreen(
  onBack: () -> Unit, onEdit: (id: Long) -> Unit, onShowRecords: (id: Long) -> Unit
) {
  composable<ProgressTrackingScreen> { navStack ->
    val progressId = navStack.toRoute<ProgressTrackingScreen>().progressId
    val dao =
      RoutineDatabase.getDatabase(LocalContext.current.applicationContext).trackedProgressDao()
    val viewmodel: ProgressTrackingScreenViewModel = viewModel(factory = viewModelFactory {
      initializer {
        ProgressTrackingScreenViewModel(
          fetchData = {
            fetchTrackedProgressFlow(fetchData = { dao.getProgressWithValuesFlow(progressId) })
          },
          addValue = { value, date ->
            saveTrackedProgressValue(
              value = TrackedProgressValue(
                id = 0L, progressId = progressId, value = value, addedDate = date
              ),
              saveData = { model -> dao.upsert(model) > 0 },
            )
          },
        )
      }
    })
    val uiState = viewmodel.uiState
    val context = LocalContext.current
    val timerTitle = stringResource(R.string.title_timer)
    val stopwatchTitle = stringResource(R.string.title_stopwatch)
    var countDownService: CountDownTimerService? by remember { mutableStateOf(null) }
    var stopwatchService: StopwatchTimerService? by remember { mutableStateOf(null) }
    val timerConnection = remember {
      object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
          // Notifications needs to be hidden here, because the service will be null in
          //  LifecycleEventEffect ON_RESUME when the configuration changes
          when (binder) {
            is CountDownTimerService.BinderHelper -> binder.getService().apply {
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

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
      countDownService?.hideNotification()
      stopwatchService?.hideNotification()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
      if (uiState.countDownTimerState.isActive.value) {
        countDownService?.showNotification(timerTitle)
      }
      if (uiState.stopwatchTimerState.isActive.value) {
        stopwatchService?.showNotification(stopwatchTitle)
      }
    }

    DisposableEffect(uiState.recordType) {
      context.apply {
        when (uiState.recordType) {
          ProgressTrackingScreenViewModel.Model.RecordType.TIMER -> CountDownTimerService::class.java
          ProgressTrackingScreenViewModel.Model.RecordType.STOPWATCH -> StopwatchTimerService::class.java
          else -> null
        }?.also {
          bindService(Intent(this, it), timerConnection, Context.BIND_AUTO_CREATE)
        }
      }

      onDispose {
        runCatching { context.unbindService(timerConnection) }
      }
    }

    ProgressTrackingScreen(
      uiState = uiState,
      onEdit = { onEdit(progressId) },
      onBack = onBack,
      onShowRecords = { onShowRecords(progressId) },
      onAddValue = { value, date -> viewmodel.addNewValue(value, date) },
      onStartTimer = { countDownService?.also { viewmodel.startTimer(it) } },
      onCancelTimer = { countDownService?.also { viewmodel.cancelTimer(it) } },
      onStartStopwatch = { stopwatchService?.also { viewmodel.startTimer(it) } },
      onStopStopwatch = { stopwatchService?.also { viewmodel.stopTimer(it) } },
      onCancelStopwatch = { stopwatchService?.also { viewmodel.cancelTimer(it) } })
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressTrackingScreen(
  uiState: ProgressTrackingScreenViewModel.UiState,
  onEdit: () -> Unit,
  onBack: () -> Unit,
  onShowRecords: () -> Unit,
  onAddValue: (value: Int, date: Date) -> Unit,
  onStartTimer: () -> Unit,
  onCancelTimer: () -> Unit,
  onStartStopwatch: () -> Unit,
  onStopStopwatch: () -> Unit,
  onCancelStopwatch: () -> Unit,
) {
  var showNewRecordDialog by remember { mutableStateOf(false) }
  var showTimerModal by remember { mutableStateOf(false) }

  if (showNewRecordDialog) {
    when (uiState.recordType) {
      ProgressTrackingScreenViewModel.Model.RecordType.STOPWATCH -> DurationRecordDialog(
        label = stringResource(R.string.dialog_title_add_new_record),
        initDuration = uiState.stopwatchTimerState.finalDuration.value,
        date = Date(),
        onConfirm = { duration, date ->
          onAddValue(duration.inWholeMilliseconds.toInt(), date)
          showNewRecordDialog = false
          uiState.stopwatchTimerState.finalDuration.update(0.seconds)
        },
        onDismiss = {
          showNewRecordDialog = false
          uiState.stopwatchTimerState.finalDuration.update(0.seconds)
        })

      else -> RepetitionRecordDialog(
        label = stringResource(R.string.dialog_title_add_new_record),
        value = 0,
        valueUnit = uiState.recordValueUnit,
        date = Date(),
        onConfirm = { value, date ->
          onAddValue(value, date)
          showNewRecordDialog = false
        },
        onDismiss = { showNewRecordDialog = false },
      )
    }
  }

  LoadingScreen(enabled = uiState.isLoading) {
    Scaffold(topBar = {
      TopAppBar(title = { Text(uiState.progressName) }, actions = {
        IconButton(onClick = onEdit) {
          Icon(
            painter = painterResource(R.drawable.rounded_edit_24),
            contentDescription = stringResource(R.string.cd_edit_tracked_progress)
          )
        }
        IconButton(enabled = uiState.records.isNotEmpty(), onClick = onShowRecords) {
          Icon(
            painter = painterResource(R.drawable.rounded_list_24),
            contentDescription = stringResource(R.string.cd_show_records)
          )
        }
      }, navigationIcon = { BackNavigationIconButton(onBack = onBack) })
    }, floatingActionButton = {
      Column {
        if (uiState.recordType == ProgressTrackingScreenViewModel.Model.RecordType.TIMER || uiState.recordType == ProgressTrackingScreenViewModel.Model.RecordType.STOPWATCH) {
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
            uiState = uiState,
            modifier = Modifier
              .fillMaxWidth()
              .sizeIn(maxHeight = 500.dp)
              .padding(16.dp)
          )
        }
      }
    }
    when (uiState.recordType) {
      ProgressTrackingScreenViewModel.Model.RecordType.TIMER -> TimerSheet(
        show = showTimerModal,
        timerTitle = stringResource(R.string.title_timer),
        timerState = uiState.countDownTimerState,
        onDismissRequest = {
          showTimerModal = false
          onCancelTimer()
        },
      ) {
        IconButton(
          onClick = {
            showTimerModal = false
            onCancelTimer()
          }, colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
          ), modifier = Modifier.size(64.dp)
        ) {
          Icon(
            painter = painterResource(R.drawable.rounded_cancel_24),
            contentDescription = stringResource(R.string.btn_cancel),
            modifier = Modifier.size(32.dp)
          )
        }
        IconButton(
          enabled = !uiState.countDownTimerState.isActive.value,
          onClick = { onStartTimer() },
          colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = .38f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = .38f),
          ),
          modifier = Modifier.size(64.dp)
        ) {
          Icon(
            painter = painterResource(R.drawable.rounded_timer_play_24),
            contentDescription = stringResource(R.string.btn_start),
            modifier = Modifier.size(32.dp)
          )
        }
      }

      ProgressTrackingScreenViewModel.Model.RecordType.STOPWATCH -> StopwatchSheet(
        show = showTimerModal,
        timerTitle = stringResource(R.string.title_stopwatch),
        timerState = uiState.stopwatchTimerState,
        onDismissRequest = {
          showTimerModal = false
          onCancelStopwatch()
        },
      ) {
        IconButton(
          onClick = {
            showTimerModal = false
            onCancelStopwatch()
          }, colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
          ), modifier = Modifier.size(64.dp)
        ) {
          Icon(
            painter = painterResource(R.drawable.rounded_cancel_24),
            contentDescription = stringResource(R.string.btn_cancel),
            modifier = Modifier.size(32.dp)
          )
        }
        ifElse(condition = uiState.stopwatchTimerState.isActive.value, ifTrue = {
          // Stop button
          IconButton(
            onClick = {
              showTimerModal = false
              onStopStopwatch()
              showNewRecordDialog = true
            }, colors = IconButtonDefaults.iconButtonColors(
              containerColor = MaterialTheme.colorScheme.secondary,
              contentColor = MaterialTheme.colorScheme.onSecondary,
              disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = .38f),
              disabledContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = .38f),
            ), modifier = Modifier.size(64.dp)
          ) {
            Icon(
              painter = painterResource(R.drawable.rounded_stop_circle_24),
              contentDescription = stringResource(R.string.btn_stop),
              modifier = Modifier.size(32.dp)
            )
          }
        }, ifFalse = {
          // Start button
          IconButton(
            onClick = {
              onStartStopwatch()
            }, colors = IconButtonDefaults.iconButtonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
              disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = .38f),
              disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = .38f),
            ), modifier = Modifier.size(64.dp)
          ) {
            Icon(
              painter = painterResource(R.drawable.rounded_timer_play_24),
              contentDescription = stringResource(R.string.btn_start),
              modifier = Modifier.size(32.dp)
            )
          }
        })
      }

      else -> null
    }
  }
}

@Composable
fun RecordChart(
  uiState: ProgressTrackingScreenViewModel.UiState, modifier: Modifier = Modifier
) {
  val values = uiState.records.map { it.toDouble() }
  val gridProperties =
    GridProperties(xAxisProperties = GridProperties.AxisProperties(lineCount = 5))
  val maxValue = (values.maxOrNull())?.let { max ->
    val segmentCount = gridProperties.yAxisProperties.lineCount - 1

    if (uiState.recordType == ProgressTrackingScreenViewModel.Model.RecordType.STOPWATCH) {
      val minutes = (segmentCount / 2).minutes.inWholeMilliseconds.toDouble()
      val multiplier = ceil(max / minutes).toInt()

      multiplier * minutes
    }
    else {
      val segment = max / segmentCount.toDouble()
      val delta = segment % 10 // Get delta to next round number

      max + segmentCount * (10 - delta)
    }
  } ?: 0.toDouble()
  val label = when (uiState.recordType) {
    ProgressTrackingScreenViewModel.Model.RecordType.STOPWATCH -> stringResource(R.string.label_time)
    else -> uiState.recordValueUnit.replaceFirstChar { char -> char.uppercase() }
  }
  val lineColor = MaterialTheme.colorScheme.primary
  val lines = remember(values) {
    listOf(
      Line(
        label = label,
        values = ifElse(
          condition = values.isNotEmpty(),
          ifTrue = { values },
          ifFalse = { listOf(0.toDouble()) }), // LineChart will crash if the values list is empty
        color = SolidColor(lineColor),
        firstGradientFillColor = lineColor.copy(alpha = .5f),
        secondGradientFillColor = Color.Transparent,
        strokeAnimationSpec = tween(1000, easing = EaseInOutCubic),
        gradientAnimationDelay = 500,
        drawStyle = DrawStyle.Stroke(width = 2.dp),
        curvedEdges = true
      )
    )
  }
  val indicatorProperties = HorizontalIndicatorProperties(
    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface), contentBuilder = { value ->
      when (uiState.recordType) {
        ProgressTrackingScreenViewModel.Model.RecordType.STOPWATCH -> value.toDuration(DurationUnit.MILLISECONDS)
          .toClockString(hasHours = value >= 1.hours.inWholeMilliseconds).trimFirst('0')

        else -> value.format(0)
      }
    })
  val labelHelperProperties = LabelHelperProperties(
    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
  )
  val popupProperties = PopupProperties(
    mode = PopupProperties.Mode.PointMode(threshold = 30.dp),
    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
    contentBuilder = { popup ->
      when (uiState.recordType) {
        ProgressTrackingScreenViewModel.Model.RecordType.STOPWATCH -> popup.value.toDuration(
          DurationUnit.MILLISECONDS
        ).toClockString(hasHours = popup.value >= 1.hours.inWholeMilliseconds).trimFirst('0')

        else -> DecimalFormat.getInstance().apply { maximumFractionDigits = 0 }.format(popup.value)
      }
    })

  LineChart(
    modifier = modifier,
    data = lines,
    indicatorProperties = indicatorProperties,
    labelHelperProperties = labelHelperProperties,
    popupProperties = popupProperties,
    maxValue = maxValue,
    gridProperties = gridProperties
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerSheet(
  show: Boolean,
  timerTitle: String,
  timerState: ProgressTrackingScreenViewModel.CountDownTimerState?,
  onDismissRequest: () -> Unit, // Needs to be here because back button will request dismiss
  content: @Composable RowScope.() -> Unit,
) {
  val isActive = timerState?.isActive?.value == true
  val duration = timerState?.duration ?: 0.seconds

  val startTime = rememberSaveable(isActive) { System.currentTimeMillis() }
  var clockText by rememberSaveable(duration) { mutableStateOf(duration.toClockString()) }
  val progress = remember {
    if (duration.inWholeMilliseconds <= 0) Animatable(1f)
    else Animatable(initialValue = ((System.currentTimeMillis() - startTime).toFloat() / duration.inWholeMilliseconds.toFloat()))
  }

  LaunchedEffect(isActive) {
    // Animate progress indicator
    if (isActive) {
      val remainingMillis = duration.inWholeMilliseconds - (System.currentTimeMillis() - startTime)

      progress.animateTo(
        targetValue = 1f, animationSpec = tween(
          durationMillis = remainingMillis.toInt(), easing = LinearEasing
        )
      )
    }
  }

  DisposableEffect(isActive) {
    // Update clock text
    val timer = if (isActive) timer(period = 1.seconds.inWholeMilliseconds) {
      val remainingMillis = duration.inWholeMilliseconds - (System.currentTimeMillis() - startTime)

      clockText = remainingMillis.milliseconds.toClockString()
    }
    else null

    onDispose {
      timer?.cancel()
      timer?.purge()
    }
  }

  GesturelessModalBottomSheet(show = show, onDismissRequest = onDismissRequest) {
    Box(
      contentAlignment = Alignment.BottomCenter, modifier = Modifier
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
      horizontalArrangement = Arrangement.SpaceEvenly,
      modifier = Modifier
        .fillMaxWidth()
        .weight(2f)
        .padding(horizontal = 16.dp)
    ) {
      content()
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchSheet(
  show: Boolean,
  timerTitle: String,
  timerState: StopwatchTimerState?,
  onDismissRequest: () -> Unit, // Needs to be here because back button will request dismiss
  content: @Composable RowScope.() -> Unit,
) {
  val isActive = timerState?.isActive?.value == true

  val startTime = rememberSaveable(isActive) { System.currentTimeMillis() }
  var clockText by rememberSaveable(isActive) {
    mutableStateOf((System.currentTimeMillis() - startTime).milliseconds.toClockString())
  }

  DisposableEffect(isActive) {
    // Update clock text
    val timer = if (isActive) timer(period = 1.seconds.inWholeMilliseconds) {
      clockText = (System.currentTimeMillis() - startTime).milliseconds.toClockString()
    }
    else null

    onDispose {
      timer?.cancel()
      timer?.purge()
    }
  }

  GesturelessModalBottomSheet(show = show, onDismissRequest = onDismissRequest) {
    Box(
      contentAlignment = Alignment.BottomCenter, modifier = Modifier
        .fillMaxWidth(.7f)
        .weight(3f)
    ) {
      Box(
        contentAlignment = Alignment.Center, modifier = Modifier.aspectRatio(1f)
      ) {
        CircularProgressIndicator(
          progress = { if (isActive) 1f else 0f },
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
      horizontalArrangement = Arrangement.SpaceEvenly,
      modifier = Modifier
        .fillMaxWidth()
        .weight(2f)
        .padding(horizontal = 16.dp)
    ) {
      content()
    }
  }
}