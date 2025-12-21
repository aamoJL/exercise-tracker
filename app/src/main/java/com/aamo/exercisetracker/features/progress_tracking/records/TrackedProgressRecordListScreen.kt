package com.aamo.exercisetracker.features.progress_tracking.records

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
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
import com.aamo.exercisetracker.features.progress_tracking.records.models.TrackedProgressRecordListModel
import com.aamo.exercisetracker.features.progress_tracking.records.use_cases.deleteTrackedProgressRecord
import com.aamo.exercisetracker.features.progress_tracking.records.use_cases.fetchTrackedProgressRecordsFlow
import com.aamo.exercisetracker.features.progress_tracking.records.use_cases.saveTrackedProgressRecord
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.components.inputs.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.modals.DeleteDialog
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.aamo.exercisetracker.utility.extensions.date.toClockString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.LocalDate
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Serializable
data class TrackedProgressRecordListScreen(val progressId: Long)

class TrackedProgressRecordListScreenViewModel(
  fetchData: () -> Flow<TrackedProgressRecordListModel>,
  private val deleteRecordData: suspend (TrackedProgressValue) -> Unit,
  private val saveRecordData: suspend (TrackedProgressValue) -> Unit,
) : ViewModel() {
  val model = fetchData().catch { }.stateIn(
    scope = viewModelScope, started = SharingStarted.Lazily, initialValue = null
  )

  fun deleteRecord(record: TrackedProgressValue) {
    viewModelScope.launch {
      runCatching { deleteRecordData(record) }
    }
  }

  fun saveRecord(record: TrackedProgressValue) {
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
          fetchData = { fetchTrackedProgressRecordsFlow(dao = dao, progressId = progressId) },
          deleteRecordData = { record -> deleteTrackedProgressRecord(dao = dao, record = record) },
          saveRecordData = { record -> saveTrackedProgressRecord(dao = dao, record = record) },
        )
      }
    })
    val model by viewmodel.model.collectAsStateWithLifecycle()

    LoadingScreen(model = model) {
      TrackedProgressRecordListScreenContent(
        model = it,
        onBack = onBack,
        onDeleteRecord = { record -> viewmodel.deleteRecord(record) },
        onSaveRecord = { record -> viewmodel.saveRecord(record) })
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackedProgressRecordListScreenContent(
  model: TrackedProgressRecordListModel,
  onBack: () -> Unit,
  onDeleteRecord: (TrackedProgressValue) -> Unit,
  onSaveRecord: (TrackedProgressValue) -> Unit,
) {
  var selectedRecord by remember { mutableStateOf<TrackedProgressValue?>(null) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  var showEditRecordDialog by remember { mutableStateOf(false) }

  DeleteDialog(
    open = showDeleteDialog,
    title = stringResource(R.string.dialog_title_delete_record),
    onDismiss = { showDeleteDialog = false },
    onConfirm = {
      selectedRecord?.also {
        onDeleteRecord(it)
      }
      showDeleteDialog = false
    })

  if (showEditRecordDialog) {
    selectedRecord?.also { record ->
      when (model.valueType) {
        TrackedProgressRecordListModel.ValueType.COUNT -> RepetitionRecordDialog(
          label = stringResource(R.string.dialog_title_edit_record),
          value = record.value, valueUnit = model.valueUnit, date = record.addedDate,
          onConfirm = { value, date ->
            onSaveRecord(record.copy(value = value, addedDate = date))
            showEditRecordDialog = false
          },
          onDismiss = { showEditRecordDialog = false },
        )

        TrackedProgressRecordListModel.ValueType.DURATION -> DurationRecordDialog(
          label = stringResource(R.string.dialog_title_edit_record),
          initDuration = record.value.toDuration(DurationUnit.MILLISECONDS),
          date = record.addedDate,
          onConfirm = { duration, date ->
            onSaveRecord(
              record.copy(value = duration.inWholeMilliseconds.toInt(), addedDate = date)
            )
            showEditRecordDialog = false
          },
          onDismiss = { showEditRecordDialog = false })
      }
    }
  }

  Scaffold(topBar = {
    TopAppBar(title = {
      Text(stringResource(R.string.title_tracked_progress_records, model.progressName))
    }, navigationIcon = { BackNavigationIconButton(onBack = onBack) })
  }) { innerPadding ->
    Surface(
      modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize()
    ) {
      LazyColumn(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier) {
        items(items = model.records, key = { it.id }) { record ->
          ListItem(headlineContent = {
            when (model.valueType) {
              TrackedProgressRecordListModel.ValueType.COUNT -> Text(text = "${record.value} ${model.valueUnit}")
              TrackedProgressRecordListModel.ValueType.DURATION -> Text(
                text = record.value.toDuration(DurationUnit.MILLISECONDS)
                  .toClockString(hasHours = true)
              )
            }
          }, overlineContent = {
            Text(text = SimpleDateFormat.getDateInstance().format(record.addedDate))
          }, trailingContent = {
            if (selectedRecord == record) {
              Row {
                IconButton(onClick = { showDeleteDialog = true }) {
                  Icon(
                    painter = painterResource(R.drawable.rounded_delete_24),
                    contentDescription = stringResource(R.string.cd_delete_record)
                  )
                }
                IconButton(onClick = { showEditRecordDialog = true }) {
                  Icon(
                    painter = painterResource(R.drawable.rounded_edit_24),
                    contentDescription = stringResource(R.string.cd_edit_record)
                  )
                }
              }
            }
          }, modifier = Modifier.clickable {
            selectedRecord = if (selectedRecord != record) record else null
          })
        }
      }
    }
  }
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun Preview() {
  ExerciseTrackerTheme {
    TrackedProgressRecordListScreenContent(
      model = TrackedProgressRecordListModel(
      progressName = "Progress 1", valueUnit = "Reps", records = listOf(
        TrackedProgressValue(
          id = 3L,
          progressId = 1L,
          value = 25,
          addedDate = Date(LocalDate.of(2025, 12, 24).toEpochDay().days.inWholeMilliseconds)
        ),
        TrackedProgressValue(
          id = 2L,
          progressId = 1L,
          value = 11,
          addedDate = Date(LocalDate.of(2025, 12, 1).toEpochDay().days.inWholeMilliseconds)
        ),
        TrackedProgressValue(
          id = 1L,
          progressId = 1L,
          value = 3,
          addedDate = Date(LocalDate.of(2025, 11, 15).toEpochDay().days.inWholeMilliseconds)
        ),
      ), valueType = TrackedProgressRecordListModel.ValueType.COUNT
    ), onBack = {}, onDeleteRecord = {}, onSaveRecord = {})
  }
}