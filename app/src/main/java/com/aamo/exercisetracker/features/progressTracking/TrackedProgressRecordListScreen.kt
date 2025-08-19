package com.aamo.exercisetracker.features.progressTracking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.DeleteDialog
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.utility.extensions.date.toClockString
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Serializable
data class TrackedProgressRecordListScreen(val progressId: Long)

class TrackedProgressRecordListScreenViewModel(
  private val fetchData: suspend () -> Model,
  private val deleteRecordData: suspend (RecordModel) -> Unit,
  private val saveRecordData: suspend (RecordModel) -> Unit,
) : ViewModel() {
  data class Model(
    val progressName: String,
    val valueUnit: String,
    val values: Flow<List<RecordModel>>,
    val valueType: ValueType
  ) {
    enum class ValueType {
      DEFAULT,
      DURATION
    }
  }

  data class RecordModel(
    val value: Int, val date: Date, val key: Long
  )

  class UiState {
    var progressName by mutableStateOf(String.EMPTY)
    var valueUnit by mutableStateOf(String.EMPTY)
    var values by mutableStateOf(emptyList<RecordModel>())
    var valueType by mutableStateOf(Model.ValueType.DEFAULT)
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
            valueType = result.valueType

            viewModelScope.launch {
              result.values.collect { list ->
                values = list.sortedByDescending { it.date }
              }
            }

            isLoading = false
          }
        }
      }.onFailure { }
    }
  }

  fun removeRecord(record: RecordModel) {
    viewModelScope.launch {
      runCatching { deleteRecordData(record) }
    }
  }

  fun saveRecord(record: RecordModel) {
    viewModelScope.launch {
      runCatching { saveRecordData(record) }
    }
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun NavGraphBuilder.trackedProgressRecordListScreen(onBack: () -> Unit) {
  composable<TrackedProgressRecordListScreen> { navStack ->
    val progressId = navStack.toRoute<TrackedProgressRecordListScreen>().progressId
    val dao =
      RoutineDatabase.getDatabase(LocalContext.current.applicationContext).trackedProgressDao()
    val viewmodel: TrackedProgressRecordListScreenViewModel = viewModel(factory = viewModelFactory {
      initializer {
        TrackedProgressRecordListScreenViewModel(
          fetchData = {
            (dao.getTrackedProgress(progressId)
              ?: throw Exception("Failed to fetch data")).let { progress ->
              TrackedProgressRecordListScreenViewModel.Model(
                progressName = progress.name,
                values = dao.getProgressValuesFlow(progressId = progressId).map { list ->
                  list.sortedBy { it.addedDate }.map {
                    TrackedProgressRecordListScreenViewModel.RecordModel(
                      value = it.value, date = it.addedDate, key = it.id
                    )
                  }
                },
                valueUnit = progress.unit,
                valueType = if (progress.hasStopWatch) TrackedProgressRecordListScreenViewModel.Model.ValueType.DURATION else TrackedProgressRecordListScreenViewModel.Model.ValueType.DEFAULT
              )
            }
          },
          deleteRecordData = { record ->
            dao.deleteValueById(trackedProgressId = record.key)
          },
          saveRecordData = { record ->
            (dao.getProgressValueById(record.key)
              ?: throw Exception("Failed to fetch data")).let { value ->
              dao.upsert(value.copy(value = record.value, addedDate = record.date))
            }
          },
        )
      }
    })
    val uiState = viewmodel.uiState

    TrackedProgressRecordListScreen(
      uiState = uiState,
      onBack = onBack,
      onDeleteRecord = { viewmodel.removeRecord(it) },
      onSaveRecord = { viewmodel.saveRecord(it) })
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackedProgressRecordListScreen(
  uiState: TrackedProgressRecordListScreenViewModel.UiState,
  onBack: () -> Unit,
  onDeleteRecord: (TrackedProgressRecordListScreenViewModel.RecordModel) -> Unit,
  onSaveRecord: (TrackedProgressRecordListScreenViewModel.RecordModel) -> Unit,
) {
  var selectedRecordKey by rememberSaveable { mutableStateOf<Long?>(null) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  var showEditRecordDialog by remember { mutableStateOf(false) }

  if (showDeleteDialog) {
    DeleteDialog(title = stringResource(R.string.dialog_title_delete_record), onDismiss = {
      showDeleteDialog = false
    }, onConfirm = {
      selectedRecordKey?.let { uiState.values.firstOrNull { it.key == selectedRecordKey } }
        ?.also { record ->
          onDeleteRecord(record)
        }
      showDeleteDialog = false
    })
  }

  if (showEditRecordDialog) {
    selectedRecordKey?.let { uiState.values.firstOrNull { it.key == selectedRecordKey } }
      ?.also { record ->
        when (uiState.valueType) {
          TrackedProgressRecordListScreenViewModel.Model.ValueType.DEFAULT -> RepetitionRecordDialog(
            label = stringResource(R.string.dialog_title_edit_record),
            value = record.value, valueUnit = uiState.valueUnit, date = record.date,
            onConfirm = { value, date ->
              onSaveRecord(record.copy(value = value, date = date))
              showEditRecordDialog = false
            },
            onDismiss = { showEditRecordDialog = false },
          )

          TrackedProgressRecordListScreenViewModel.Model.ValueType.DURATION -> DurationRecordDialog(
            label = stringResource(R.string.dialog_title_edit_record),
            initDuration = record.value.toDuration(DurationUnit.MILLISECONDS),
            date = record.date,
            onConfirm = { duration, date ->
              onSaveRecord(record.copy(value = duration.inWholeMilliseconds.toInt(), date = date))
              showEditRecordDialog = false
            },
            onDismiss = { showEditRecordDialog = false })
        }
      }
  }

  LoadingScreen(enabled = uiState.isLoading) {
    Scaffold(topBar = {
      TopAppBar(title = {
        Text(stringResource(R.string.title_tracked_progress_records, uiState.progressName))
      }, navigationIcon = { BackNavigationIconButton(onBack = onBack) })
    }) { innerPadding ->
      Surface(
        modifier = Modifier
          .padding(innerPadding)
          .fillMaxSize()
      ) {
        LazyColumn(
          horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
        ) {
          items(items = uiState.values, key = { it.key }) { item ->
            ListItem(headlineContent = {
              when (uiState.valueType) {
                TrackedProgressRecordListScreenViewModel.Model.ValueType.DEFAULT -> Text(text = "${item.value} ${uiState.valueUnit}")
                TrackedProgressRecordListScreenViewModel.Model.ValueType.DURATION -> Text(
                  text = item.value.toDuration(DurationUnit.MILLISECONDS)
                    .toClockString(hasHours = true)
                )
              }
            }, overlineContent = {
              Text(text = SimpleDateFormat.getDateInstance().format(item.date))
            }, trailingContent = {
              if (selectedRecordKey == item.key) {
                Row {
                  IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                      imageVector = Icons.Filled.Delete,
                      contentDescription = stringResource(R.string.cd_delete_record)
                    )
                  }
                  IconButton(onClick = { showEditRecordDialog = true }) {
                    Icon(
                      imageVector = Icons.Filled.Edit,
                      contentDescription = stringResource(R.string.cd_edit_record)
                    )
                  }
                }
              }
            }, modifier = Modifier.clickable {
              selectedRecordKey = if (selectedRecordKey != item.key) item.key else null
            })
          }
        }
      }
    }
  }
}