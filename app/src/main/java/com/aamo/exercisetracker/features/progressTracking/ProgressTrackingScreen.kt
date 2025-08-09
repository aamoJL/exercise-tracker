package com.aamo.exercisetracker.features.progressTracking

import android.icu.text.DecimalFormat
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
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
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.utility.extensions.date.toClockString
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.extensions.general.trimFirst
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
import kotlin.math.ceil
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
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
    val recordType: RecordType
  ) {
    enum class RecordType {
      REPETITION,
      TIMER,
      STOPWATCH
    }
  }

  class UiState {
    var progressName by mutableStateOf(String.EMPTY)
    var recordValueUnit by mutableStateOf(String.EMPTY)
    var records by mutableStateOf(emptyList<Int>())
    var recordType by mutableStateOf(Model.RecordType.REPETITION)
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
                })
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

    ProgressTrackingScreen(
      uiState = uiState,
      onEdit = { onEdit(progressId) },
      onBack = onBack,
      onShowRecords = { onShowRecords(progressId) },
      onAddValue = { value, date -> viewmodel.addNewValue(value, date) },
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
  onAddValue: (value: Int, date: Date) -> Unit
) {
  var showNewRecordDialog by remember { mutableStateOf(false) }

  if (showNewRecordDialog) {
    if (uiState.recordType != ProgressTrackingScreenViewModel.Model.RecordType.STOPWATCH) {
      RepetitionRecordDialog(
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
    else {
      DurationRecordDialog(
        label = stringResource(R.string.dialog_title_add_new_record),
        duration = Duration.ZERO,
        date = Date(),
        onConfirm = { duration, date ->
          onAddValue(duration.inWholeMilliseconds.toInt(), date)
          showNewRecordDialog = false
        },
        onDismiss = { showNewRecordDialog = false })
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
      FloatingActionButton(
        shape = CircleShape,
        containerColor = ButtonDefaults.buttonColors().containerColor,
        onClick = { showNewRecordDialog = true },
        modifier = Modifier.padding(8.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add))
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
          // TODO: show timer?
        }
      }
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