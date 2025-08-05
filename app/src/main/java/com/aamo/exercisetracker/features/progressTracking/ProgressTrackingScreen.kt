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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.aamo.exercisetracker.ui.components.IntNumberField
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class ProgressTrackingScreen(val progressId: Long)

class ProgressTrackingScreenViewModel(
  private val fetchData: suspend () -> Model,
  private val addValue: suspend (value: Int) -> Unit,
) : ViewModel() {
  data class Model(
    val progressName: String, val valueUnit: String, val values: Flow<List<Int>>
  )

  class UiState {
    var progressName by mutableStateOf(String.EMPTY)
    var valueUnit by mutableStateOf(String.EMPTY)
    var values by mutableStateOf(emptyList<Int>())
    var isLoading by mutableStateOf(true)
  }

  val uiState = UiState()

  init {
    viewModelScope.launch {
      runCatching {
        fetchData().let { result ->
          uiState.apply {
            progressName = result.progressName
            valueUnit = result.valueUnit

            viewModelScope.launch {
              result.values.collect {
                values = it
              }
            }

            isLoading = false
          }
        }
      }.onFailure { }
    }
  }

  fun addNewValue(value: Int) {
    viewModelScope.launch {
      addValue(value)
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
                values = dao.getProgressValuesFlow(progressId = progressId)
                  .map { list -> list.map { it.value } },
                valueUnit = progress.unit
              )
            }
          },
          addValue = {
            dao.upsert(
              TrackedProgressValue(progressId = progressId, value = it, addedDate = Date())
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
      onAddValue = { viewmodel.addNewValue(it) },
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
  onAddValue: (value: Int) -> Unit
) {
  var showNewRecordDialog by remember { mutableStateOf(false) }

  if (showNewRecordDialog) {
    NewRecordDialog(
      onConfirm = {
      onAddValue(it)
      showNewRecordDialog = false
    }, onDismiss = { showNewRecordDialog = false }, valueUnit = uiState.valueUnit)
  }

  LoadingScreen(enabled = uiState.isLoading) {
    // TODO: show timer?
    Scaffold(topBar = {
      TopAppBar(title = { Text(uiState.progressName) }, actions = {
        IconButton(onClick = onEdit) {
          Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = stringResource(R.string.cd_edit_tracked_progress)
          )
        }
        IconButton(enabled = uiState.values.isNotEmpty(), onClick = onShowRecords) {
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
          ValuesChart(
            label = uiState.valueUnit,
            values = uiState.values.map { it.toDouble() },
            modifier = Modifier
              .fillMaxWidth()
              .sizeIn(maxHeight = 500.dp)
              .padding(16.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun NewRecordDialog(
  onConfirm: (value: Int) -> Unit, onDismiss: () -> Unit, valueUnit: String
) {
  val focusRequester = remember { FocusRequester() }
  var fieldValue by remember { mutableIntStateOf(0) }

  LaunchedEffect(Unit) {
    coroutineContext.job.invokeOnCompletion {
      focusRequester.requestFocus()
    }
  }

  AlertDialog(title = { Text(stringResource(R.string.dialog_title_add_new_record)) }, text = {
    IntNumberField(
      value = fieldValue,
      onValueChange = { fieldValue = it },
      suffix = { Text(text = valueUnit) },
      modifier = Modifier.focusRequester(focusRequester)
    )
  }, onDismissRequest = onDismiss, confirmButton = {
    TextButton(onClick = { onConfirm(fieldValue) }) {
      Text(text = stringResource(R.string.btn_add))
    }
  }, dismissButton = {
    TextButton(onClick = onDismiss) {
      Text(text = stringResource(R.string.btn_cancel))
    }
  })
}

@Composable
fun ValuesChart(label: String, values: List<Double>, modifier: Modifier = Modifier) {
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
    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
  )
  val labelHelperProperties = LabelHelperProperties(
    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
  )
  val popupProperties = PopupProperties(
    mode = PopupProperties.Mode.PointMode(threshold = 30.dp),
    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
    contentBuilder = { _, _, value ->
      DecimalFormat.getInstance().apply { maximumFractionDigits = 0 }.format(value)
    })

  LineChart(
    modifier = modifier,
    data = lines,
    indicatorProperties = indicatorProperties,
    labelHelperProperties = labelHelperProperties,
    popupProperties = popupProperties
  )
}