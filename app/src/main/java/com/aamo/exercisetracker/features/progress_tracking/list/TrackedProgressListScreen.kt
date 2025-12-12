package com.aamo.exercisetracker.features.progress_tracking.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.RoutineDatabase
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.list.use_cases.deleteTrackedProgresses
import com.aamo.exercisetracker.features.progress_tracking.list.use_cases.fetchTrackedProgressesFlow
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.components.inputs.text_field.SearchTextField
import com.aamo.exercisetracker.ui.components.modals.DeleteDialog
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object TrackedProgressListScreen

class TrackedProgressListScreenViewModel(
  fetchData: () -> Flow<List<TrackedProgress>>,
  private val deleteData: suspend (List<TrackedProgress>) -> Unit,
) : ViewModel() {
  private var _selections = MutableStateFlow<List<TrackedProgress>>(emptyList())
  val selections = _selections.asStateFlow()

  private var _filterWord = MutableStateFlow(String.EMPTY)
  val filterWord = _filterWord.asStateFlow()

  val filteredProgresses = combine(fetchData(), filterWord) { progress, word ->
    progress.filter { it.name.contains(word, ignoreCase = true) }
  }.stateIn(scope = viewModelScope, started = SharingStarted.Lazily, initialValue = null)

  fun setFilterWord(word: String) {
    _filterWord.update { word }
  }

  fun switchProgressSelection(models: List<TrackedProgress>, state: Boolean) {
    _selections.update { list ->
      list.toMutableList().apply {
        if (state) this.addAll(models.filter { !this.contains(it) })
        else this.removeAll(models)
      }
    }
  }

  fun deleteProgresses(progresses: List<TrackedProgress>) {
    if (progresses.isEmpty()) return

    viewModelScope.launch {
      runCatching { deleteData(progresses) }
    }
  }
}

fun NavGraphBuilder.trackedProgressListScreen(
  onSelectProgress: (id: Long) -> Unit, onAddProgress: () -> Unit
) {
  composable<TrackedProgressListScreen> {
    val dao =
      RoutineDatabase.getDatabase(LocalContext.current.applicationContext).trackedProgressDao()
    val viewmodel: TrackedProgressListScreenViewModel = viewModel(factory = viewModelFactory {
      initializer {
        TrackedProgressListScreenViewModel(
          fetchData = { fetchTrackedProgressesFlow(dao = dao) },
          deleteData = { models -> deleteTrackedProgresses(dao = dao, *models.toTypedArray()) })
      }
    })
    val progresses by viewmodel.filteredProgresses.collectAsStateWithLifecycle()
    val filterWord by viewmodel.filterWord.collectAsStateWithLifecycle()
    val selections by viewmodel.selections.collectAsStateWithLifecycle()

    TrackedProgressListScreenContent(
      progresses = progresses ?: emptyList(),
      filterWord = filterWord,
      selections = selections,
      isLoading = progresses == null,
      onFilterChanged = { viewmodel.setFilterWord(it) },
      onSelectProgress = onSelectProgress,
      onAdd = onAddProgress,
      onDeleteProgresses = { viewmodel.deleteProgresses(it) },
      onSwitchSelection = { models, state -> viewmodel.switchProgressSelection(models, state) },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackedProgressListScreenContent(
  progresses: List<TrackedProgress>,
  filterWord: String,
  selections: List<TrackedProgress>,
  isLoading: Boolean,
  onFilterChanged: (String) -> Unit,
  onSelectProgress: (id: Long) -> Unit,
  onAdd: () -> Unit,
  onDeleteProgresses: (List<TrackedProgress>) -> Unit,
  onSwitchSelection: (List<TrackedProgress>, Boolean) -> Unit
) {
  var openDeleteDialog by remember { mutableStateOf(false) }

  DeleteDialog(
    open = openDeleteDialog,
    title = stringResource(R.string.dialog_title_delete_tracked_progresses),
    onDismiss = { openDeleteDialog = false },
    onConfirm = {
      openDeleteDialog = false
      onDeleteProgresses(selections)
    })

  BackHandler(enabled = selections.isNotEmpty()) {
    onSwitchSelection(progresses, false)
  }

  Surface {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .imePadding()
    ) {
      if (selections.isNotEmpty()) SelectionTopBar(
        selectionCount = selections.size, onDeleteSelected = { openDeleteDialog = true })
      else UnselectionTopBar(
        filterWord = filterWord, onFilterChanged = onFilterChanged, onAdd = onAdd
      )
      LoadingScreen(loading = isLoading) {
        LazyColumn(
          userScrollEnabled = true,
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier
            .padding(top = 4.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(8.dp))
        ) {
          items(progresses) { model ->
            val selected = selections.contains(model)

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceVariant)
                .combinedClickable(onClick = {
                  if (selections.isEmpty()) onSelectProgress(model.id)
                  else onSwitchSelection(listOf(model), !selected)
                }, onLongClick = {
                  if (selections.isEmpty()) onSwitchSelection(listOf(model), true)
                })
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)
              ) {
                if (selections.isNotEmpty()) {
                  Checkbox(checked = selected, onCheckedChange = null)
                }
                Text(text = model.name, fontWeight = FontWeight.Bold)
              }
              Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier
                  .fillMaxSize()
                  .padding(horizontal = 12.dp, vertical = 8.dp)
              ) {
                IntervalTrailing(intervalWeeks = model.intervalWeeks)
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnselectionTopBar(
  filterWord: String,
  onFilterChanged: (String) -> Unit,
  onAdd: () -> Unit,
) {
  TopAppBar(title = { }, actions = {
    SearchTextField(
      value = filterWord,
      placeholder = stringResource(R.string.ph_search),
      onValueChange = onFilterChanged
    )
    IconButton(onClick = onAdd) {
      Icon(
        painter = painterResource(R.drawable.rounded_add_24),
        contentDescription = stringResource(R.string.cd_add_tracked_progress)
      )
    }
  })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(selectionCount: Int, onDeleteSelected: () -> Unit) {
  TopAppBar(title = {
    Text(text = stringResource(R.string.x_count_selected, selectionCount))
  }, actions = {
    IconButton(onClick = onDeleteSelected) {
      Icon(
        painter = painterResource(R.drawable.rounded_delete_24),
        contentDescription = stringResource(R.string.cd_delete_tracked_progress)
      )
    }
  })
}

@Composable
private fun IntervalTrailing(intervalWeeks: Int) {
  val color = when (intervalWeeks) {
    0 -> MaterialTheme.colorScheme.outline
    else -> MaterialTheme.colorScheme.secondary
  }
  val text = when (intervalWeeks) {
    0 -> stringResource(R.string.label_untimed)
    1 -> stringResource(R.string.label_weekly)
    else -> stringResource(R.string.label_every_x_weeks, intervalWeeks)
  }

  Text(text = text, color = color, style = MaterialTheme.typography.labelSmall)
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun Preview() {
  ExerciseTrackerTheme(darkTheme = true) {
    TrackedProgressListScreenContent(
      progresses = listOf(
      TrackedProgress(id = 1, name = "Progress 1"),
      TrackedProgress(id = 2, name = "Progress 2"),
      TrackedProgress(id = 3, name = "Progress 3"),
    ),
      filterWord = String.EMPTY,
      selections = listOf(
        TrackedProgress(id = 1, name = "Progress 1"),
      ),
      isLoading = false,
      onFilterChanged = {},
      onSelectProgress = {},
      onAdd = {},
      onDeleteProgresses = {},
      onSwitchSelection = { _, _ -> })
  }
}