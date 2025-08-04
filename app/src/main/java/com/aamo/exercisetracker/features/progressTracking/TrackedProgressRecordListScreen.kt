package com.aamo.exercisetracker.features.progressTracking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.DeleteDialog
import com.aamo.exercisetracker.ui.components.IntNumberField
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date

@Serializable
data class TrackedProgressRecordListScreen(val progressId: Long)

class TrackedProgressRecordListScreenViewModel(
  private val fetchData: suspend () -> Model,
  private val deleteRecordData: suspend (RecordModel) -> Unit,
  private val saveRecordData: suspend (RecordModel) -> Unit,
) : ViewModel() {
  data class Model(
    val progressName: String, val valueUnit: String, val values: Flow<List<RecordModel>>
  )

  data class RecordModel(
    val value: Int, val date: Date, val key: Long
  )

  class UiState {
    var progressName by mutableStateOf(String.EMPTY)
    var valueUnit by mutableStateOf(String.EMPTY)
    var values by mutableStateOf(emptyList<RecordModel>())
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
                valueUnit = progress.unit
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
        EditRecordDialog(
          record = record, onConfirm = {
          onSaveRecord(it)
          showEditRecordDialog = false
        }, onDismiss = { showEditRecordDialog = false }, valueUnit = uiState.valueUnit)
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
              Text(text = "${item.value} ${uiState.valueUnit}")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditRecordDialog(
  record: TrackedProgressRecordListScreenViewModel.RecordModel,
  valueUnit: String,
  onConfirm: (TrackedProgressRecordListScreenViewModel.RecordModel) -> Unit,
  onDismiss: () -> Unit
) {
  var showDatePicker by remember { mutableStateOf(false) }
  var fieldValue by remember { mutableIntStateOf(record.value) }
  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = record.date.time)

  AlertDialog(title = { Text(stringResource(R.string.dialog_title_edit_record)) }, text = {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      IntNumberField(
        value = fieldValue,
        onValueChange = { fieldValue = it },
        suffix = { Text(text = valueUnit) })
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = SimpleDateFormat.getDateInstance()
          .format(datePickerState.selectedDateMillis?.let { Date(it) } ?: record.date))
        IconButton(
          onClick = { showDatePicker = true }, colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.tertiary
          )
        ) {
          Icon(
            imageVector = Icons.Filled.DateRange,
            contentDescription = stringResource(R.string.cd_change_date)
          )
        }
      }
    }
  }, onDismissRequest = onDismiss, confirmButton = {
    TextButton(onClick = {
      onConfirm(
        record.copy(
          value = fieldValue,
        date = datePickerState.selectedDateMillis?.let { Date(it) } ?: record.date))
    }) {
      Text(text = stringResource(R.string.btn_save))
    }
  }, dismissButton = {
    TextButton(onClick = onDismiss) {
      Text(text = stringResource(R.string.btn_cancel))
    }
  })

  if (showDatePicker) {
    val initDate = remember { datePickerState.selectedDateMillis }

    DatePickerDialog(onDismissRequest = {
      showDatePicker = false
      datePickerState.selectedDateMillis = initDate
    }, confirmButton = {
      TextButton(onClick = { showDatePicker = false }) {
        Text(text = stringResource(R.string.btn_select))
      }
    }, dismissButton = {
      TextButton(onClick = {
        showDatePicker = false
        datePickerState.selectedDateMillis = initDate
      }) {
        Text(text = stringResource(R.string.btn_cancel))
      }
    }) {
      DatePicker(state = datePickerState)
    }
  }
}