package com.aamo.exercisetracker.features.progressTracking

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
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
import com.aamo.exercisetracker.services.CountDownTimerService
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
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
import kotlinx.coroutines.flow.map
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
  private val fetchData: suspend () -> Model,
  private val addValue: suspend (value: Int, date: Date) -> Unit,
) : ViewModel() {
  data class Model(
    val progressName: String,
    val recordValueUnit: String,
    val records: Flow<List<Int>>,
    val recordType: RecordType,
    val countDownTime: Duration?
  ) {
    enum class RecordType {
      REPETITION,
      TIMER,
      STOPWATCH
    }
  }

  class TimerState(val duration: Duration) {
    val isActive =
      ViewModelState(false).validation { value -> value && duration > 0.seconds }.onChange {
        if (it) isFinished = false
      }
    var isFinished by mutableStateOf(false)
  }

  class UiState {
    var progressName by mutableStateOf(String.EMPTY)
    var recordValueUnit by mutableStateOf(String.EMPTY)
    var records by mutableStateOf(emptyList<Int>())
    var recordType by mutableStateOf(Model.RecordType.REPETITION)
    var timerState: TimerState? = TimerState(0.milliseconds)
    val stopwatchActive = ViewModelState(false)
    var isLoading by mutableStateOf(true)
  }

  val uiState = UiState()

  init {
    viewModelScope.launch {
      runCatching {
        fetchData().let { result ->
          uiState.apply {
            progressName = result.progressName
            recordValueUnit = result.recordValueUnit
            recordType = result.recordType
            result.countDownTime?.also {
              timerState = TimerState(duration = result.countDownTime)
            }

            viewModelScope.launch {
              result.records.collect {
                records = it
              }
            }

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

    uiState.timerState?.let { timerState ->
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

  fun cancelTimer(countDownTimerService: CountDownTimerService) {
    countDownTimerService.cancel()
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
            (dao.getTrackedProgress(progressId)
              ?: throw Exception("Failed to fetch data")).let { progress ->
              ProgressTrackingScreenViewModel.Model(
                progressName = progress.name,
                records = dao.getProgressValuesFlow(progressId = progressId)
                  .map { list -> list.sortedBy { it.addedDate }.map { it.value } },
                recordValueUnit = progress.unit,
                recordType = when {
                  progress.hasStopWatch -> ProgressTrackingScreenViewModel.Model.RecordType.STOPWATCH
                  progress.timerTime?.let { it > 0 } == true -> ProgressTrackingScreenViewModel.Model.RecordType.TIMER
                  else -> ProgressTrackingScreenViewModel.Model.RecordType.REPETITION
                },
                countDownTime = progress.timerTime?.milliseconds)
            }
          },
          addValue = { value, date ->
            dao.upsert(
              TrackedProgressValue(progressId = progressId, value = value, addedDate = date)
            )
          },
        )
      }
    })
    val uiState = viewmodel.uiState

    val context = LocalContext.current
    val timerTitle = stringResource(R.string.title_timer)
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

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
      timerService?.hideNotification()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
      if (uiState.timerState?.isActive?.value == true) {
        timerService?.showNotification(timerTitle)
      }
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

    ProgressTrackingScreen(
      uiState = uiState,
      onEdit = { onEdit(progressId) },
      onBack = onBack,
      onShowRecords = { onShowRecords(progressId) },
      onAddValue = { value, date -> viewmodel.addNewValue(value, date) },
      onStartTimer = { timerService?.also { viewmodel.startTimer(it) } },
      onCancelTimer = { timerService?.also { viewmodel.cancelTimer(it) } },
    )
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
) {
  var showNewRecordDialog by remember { mutableStateOf(false) }
  var showTimerSheet by remember { mutableStateOf(false) }
  val timerSheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true,
    confirmValueChange = { false /* Prevents closing by pressing outside the sheet */ })

  if (showNewRecordDialog) {
    when (uiState.recordType) {
      ProgressTrackingScreenViewModel.Model.RecordType.STOPWATCH -> DurationRecordDialog(
        label = stringResource(R.string.dialog_title_add_new_record),
        duration = Duration.ZERO,
        date = Date(),
        onConfirm = { duration, date ->
          onAddValue(duration.inWholeMilliseconds.toInt(), date)
          showNewRecordDialog = false
        },
        onDismiss = { showNewRecordDialog = false })

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

  LaunchedEffect(uiState.timerState?.isFinished) {
    if (uiState.timerState?.isFinished == true) {
      showTimerSheet = false
      showNewRecordDialog = true
    }
  }

  LoadingScreen(enabled = uiState.isLoading) {
    Scaffold(topBar = {
      TopAppBar(title = { Text(uiState.progressName) }, actions = {
        IconButton(onClick = onEdit) {
          Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = stringResource(R.string.cd_edit_tracked_progress)
          )
        }
        IconButton(enabled = uiState.records.isNotEmpty(), onClick = onShowRecords) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.List,
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
            onClick = { showTimerSheet = true },
            modifier = Modifier.padding(8.dp)
          ) {
            Icon(
              painter = painterResource(R.drawable.outline_timer_24),
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
          Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add))
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
        isVisible = showTimerSheet,
        timerTitle = stringResource(R.string.title_timer),
        timerState = uiState.timerState,
        sheetState = timerSheetState,
      ) {
        Button(
          onClick = {
            showTimerSheet = false
            onCancelTimer()
          }, colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
          ), modifier = Modifier.weight(1f)
        ) {
          Text(stringResource(R.string.btn_cancel))
        }
        Button(
          onClick = onStartTimer, colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary
          ), modifier = Modifier.weight(1f)
        ) {
          Text(stringResource(R.string.btn_start))
        }
      }

      ProgressTrackingScreenViewModel.Model.RecordType.STOPWATCH -> StopwatchSheet(
        isVisible = showTimerSheet,
        isActive = uiState.stopwatchActive.value,
        timerTitle = stringResource(R.string.title_stopwatch),
        sheetState = timerSheetState,
      ) {
        Button(
          onClick = { showTimerSheet = false }, colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
          ), modifier = Modifier.weight(1f)
        ) {
          Text(stringResource(R.string.btn_cancel))
        }
        Button(
          onClick = { TODO("start stopwatch") }, colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary
          ), modifier = Modifier.weight(1f)
        ) {
          Text(stringResource(R.string.btn_start))
        }
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
    contentBuilder = { _, _, value ->
      when (uiState.recordType) {
        ProgressTrackingScreenViewModel.Model.RecordType.STOPWATCH -> value.toDuration(DurationUnit.MILLISECONDS)
          .toClockString(hasHours = value >= 1.hours.inWholeMilliseconds).trimFirst('0')

        else -> DecimalFormat.getInstance().apply { maximumFractionDigits = 0 }.format(value)
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
  isVisible: Boolean,
  timerTitle: String,
  timerState: ProgressTrackingScreenViewModel.TimerState?,
  sheetState: SheetState,
  content: @Composable RowScope.() -> Unit,
) {
  if (isVisible) {
    val isActive = timerState?.isActive?.value == true
    val duration = timerState?.duration ?: 0.seconds

    val startTime = rememberSaveable(isActive) { System.currentTimeMillis() }
    var clockText by rememberSaveable { mutableStateOf(duration.toClockString()) }
    val progress = remember {
      if (duration.inWholeMilliseconds <= 0) Animatable(1f)
      else Animatable(initialValue = ((System.currentTimeMillis() - startTime).toFloat() / duration.inWholeMilliseconds.toFloat()))
    }

    LaunchedEffect(isActive) {
      if (isActive) {
        val remainingMillis =
          duration.inWholeMilliseconds - (System.currentTimeMillis() - startTime)

        progress.animateTo(
          targetValue = 1f, animationSpec = tween(
            durationMillis = remainingMillis.toInt(), easing = LinearEasing
          )
        )
      }
    }

    DisposableEffect(isActive) {
      val timer = if (isActive) timer(period = 1.seconds.inWholeMilliseconds) {
        val remainingMillis =
          duration.inWholeMilliseconds - (System.currentTimeMillis() - startTime)

        clockText = remainingMillis.milliseconds.toClockString()
      }
      else null

      onDispose {
        timer?.cancel()
        timer?.purge()
      }
    }

    ModalBottomSheet(sheetState = sheetState, onDismissRequest = {}, dragHandle = null) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchSheet(
  isVisible: Boolean,
  isActive: Boolean,
  timerTitle: String,
  sheetState: SheetState,
  content: @Composable RowScope.() -> Unit,
) {
  if (isVisible) {
    val startTime = rememberSaveable(isActive) { System.currentTimeMillis() }
    var passedDuration by rememberSaveable { mutableLongStateOf(0L) }
    var clockText by rememberSaveable(passedDuration) { mutableStateOf(passedDuration.milliseconds.toClockString()) }

    DisposableEffect(isActive) {
      val timer = if (isActive) timer(period = 1.seconds.inWholeMilliseconds) {
        passedDuration = System.currentTimeMillis() - startTime
      }
      else null

      onDispose {
        timer?.cancel()
        timer?.purge()
      }
    }

    ModalBottomSheet(
      sheetState = sheetState, onDismissRequest = {}, dragHandle = null
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