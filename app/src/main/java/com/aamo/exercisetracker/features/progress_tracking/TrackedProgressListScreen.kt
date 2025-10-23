package com.aamo.exercisetracker.features.progress_tracking

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
import com.aamo.exercisetracker.features.progress_tracking.use_cases.deleteTrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.use_cases.fromDao
import com.aamo.exercisetracker.ui.components.DeleteDialog
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.components.SearchTextField
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
  private val fetchData: () -> Flow<List<ProgressModel>>,
  private val deleteData: suspend (List<TrackedProgress>) -> Boolean,
) : ViewModel() {
  data class ProgressModel(
    val progress: TrackedProgress, val isSelected: Boolean
  ) {
    companion object
  }

  init {
    viewModelScope.launch {
      runCatching {
        fetchData().collect { list ->
          _progresses.update { list.sortedBy { it.progress.name } }
          isLoading = false
        }
      }
    }
  }

  var isLoading by mutableStateOf(true)
    private set

  private val _progresses = MutableStateFlow<List<ProgressModel>>(emptyList())

  private var _filterWord = MutableStateFlow(String.EMPTY)
  val filterWord = _filterWord.asStateFlow()

  val filteredProgresses = combine(_progresses, filterWord) { progress, word ->
    progress.filter { it.progress.name.contains(word, ignoreCase = true) }
  }.stateIn(scope = viewModelScope, started = SharingStarted.Lazily, initialValue = emptyList())

  fun setFilterWord(word: String) {
    _filterWord.update { word }
  }

  fun switchProgressSelection(models: List<ProgressModel>, state: Boolean) {
    _progresses.update { list ->
      list.toMutableList().apply {
        models.forEach { model ->
          this.indexOf(model).also { index ->
            if (index != -1) {
              this[index] = this[index].copy(isSelected = state)
            }
          }
        }
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
        TrackedProgressListScreenViewModel(fetchData = {
          TrackedProgressListScreenViewModel.ProgressModel.fromDao {
            dao.getProgressesFlow()
          }
        }, deleteData = { progresses ->
          deleteTrackedProgress(*progresses.toTypedArray()) {
            dao.delete(*progresses.toTypedArray()) > 0
          }
        })
      }
    })
    val progresses by viewmodel.filteredProgresses.collectAsStateWithLifecycle()
    val filterWord by viewmodel.filterWord.collectAsStateWithLifecycle()

    TrackedProgressListScreen(
      progresses = progresses,
      filterWord = filterWord,
      isLoading = viewmodel.isLoading,
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
fun TrackedProgressListScreen(
  progresses: List<TrackedProgressListScreenViewModel.ProgressModel>,
  filterWord: String,
  isLoading: Boolean,
  onFilterChanged: (String) -> Unit,
  onSelectProgress: (id: Long) -> Unit,
  onAdd: () -> Unit,
  onDeleteProgresses: (List<TrackedProgress>) -> Unit,
  onSwitchSelection: (List<TrackedProgressListScreenViewModel.ProgressModel>, Boolean) -> Unit
) {
  val itemsSelected by remember(progresses) { mutableStateOf(progresses.any { it.isSelected }) }
  var openDeleteDialog by remember { mutableStateOf(false) }

  if (openDeleteDialog) {
    DeleteDialog(
      title = stringResource(R.string.dialog_title_delete_tracked_progresses),
      onDismiss = { openDeleteDialog = false },
      onConfirm = {
        openDeleteDialog = false
        onDeleteProgresses(progresses.filter { it.isSelected }.map { it.progress })
      })
  }

  BackHandler(enabled = itemsSelected) {
    onSwitchSelection(progresses, false)
  }

  Surface {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .imePadding()
    ) {
      if (itemsSelected) SelectionTopBar(
        selectionCount = progresses.count { it.isSelected },
        onDeleteSelected = { openDeleteDialog = true })
      else UnselectionTopBar(
        filterWord = filterWord, onFilterChanged = onFilterChanged, onAdd = onAdd
      )
      LoadingScreen(enabled = isLoading) {
        LazyColumn(
          userScrollEnabled = true,
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier
            .padding(top = 4.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(8.dp))
        ) {
          items(progresses) { model ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceVariant)
                .combinedClickable(onClick = {
                  if (!itemsSelected) onSelectProgress(model.progress.id)
                  else onSwitchSelection(listOf(model), !model.isSelected)
                }, onLongClick = { if (!itemsSelected) onSwitchSelection(listOf(model), true) })
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)
              ) {
                if (itemsSelected) {
                  Checkbox(checked = model.isSelected, onCheckedChange = null)
                }
                Text(text = model.progress.name, fontWeight = FontWeight.Bold)
              }
              Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier
                  .fillMaxSize()
                  .padding(horizontal = 12.dp, vertical = 8.dp)
              ) {
                IntervalTrailing(intervalWeeks = model.progress.intervalWeeks)
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
  TopAppBar(title = { null }, actions = {
    SearchTextField(value = filterWord, onValueChange = onFilterChanged)
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