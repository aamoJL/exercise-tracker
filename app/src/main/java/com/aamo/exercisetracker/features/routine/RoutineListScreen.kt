package com.aamo.exercisetracker.features.routine

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
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.features.routine.use_cases.deleteRoutine
import com.aamo.exercisetracker.features.routine.use_cases.fromDao
import com.aamo.exercisetracker.ui.components.DeleteDialog
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.components.SearchTextField
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOrder
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Calendar

@Serializable
object RoutineListScreen

class RoutineListScreenViewModel(
  private val fetchData: () -> Flow<List<RoutineModel>>,
  private val deleteData: suspend (List<Routine>) -> Boolean
) : ViewModel() {
  data class RoutineModel(
    val routine: Routine, val schedule: RoutineSchedule?, val isSelected: Boolean = false
  ) {
    companion object
  }

  init {
    viewModelScope.launch {
      runCatching {
        fetchData().collect { list ->
          _routines.update { list.sortedBy { it.routine.name } }
          isLoading = false
        }
      }
    }
  }

  var isLoading by mutableStateOf(true)
    private set

  private var _routines = MutableStateFlow<List<RoutineModel>>(emptyList())

  private var _filterWord = MutableStateFlow(String.EMPTY)
  val filterWord = _filterWord.asStateFlow()

  val filteredRoutines = combine(_routines, filterWord) { routine, word ->
    routine.filter { it.routine.name.contains(word, ignoreCase = true) }
  }.stateIn(scope = viewModelScope, started = SharingStarted.Lazily, initialValue = emptyList())

  fun setFilterWord(word: String) {
    _filterWord.update { word }
  }

  fun switchRoutineSelection(models: List<RoutineModel>, state: Boolean) {
    _routines.update { list ->
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

  fun deleteRoutines(routines: List<Routine>) {
    if (routines.isEmpty()) return

    viewModelScope.launch {
      runCatching { deleteData(routines) }
    }
  }
}

fun NavGraphBuilder.routineListScreen(
  onSelectRoutine: (id: Long) -> Unit, onAddRoutine: () -> Unit
) {
  composable<RoutineListScreen> {
    val dao = RoutineDatabase.getDatabase(LocalContext.current.applicationContext).routineDao()
    val viewmodel: RoutineListScreenViewModel = viewModel(factory = viewModelFactory {
      initializer {
        RoutineListScreenViewModel(
          fetchData = {
            RoutineListScreenViewModel.RoutineModel.fromDao {
              dao.getRoutinesWithScheduleFlow()
            }
          },
          deleteData = { routines ->
            deleteRoutine(*routines.toTypedArray()) {
              dao.delete(*it.toTypedArray()) > 0
            }
          },
        )
      }
    })
    val routines by viewmodel.filteredRoutines.collectAsStateWithLifecycle()
    val filterWord by viewmodel.filterWord.collectAsStateWithLifecycle()

    RoutineListScreen(
      routines = routines,
      filterWord = filterWord,
      isLoading = viewmodel.isLoading,
      onSelectRoutine = onSelectRoutine,
      onFilterChanged = { viewmodel.setFilterWord(it) },
      onAdd = onAddRoutine,
      onDeleteRoutines = { viewmodel.deleteRoutines(it) },
      onSwitchSelection = { models, state ->
        viewmodel.switchRoutineSelection(models, state)
      },
    )
  }
}

@Composable
fun RoutineListScreen(
  routines: List<RoutineListScreenViewModel.RoutineModel>,
  filterWord: String,
  isLoading: Boolean,
  onFilterChanged: (String) -> Unit,
  onSelectRoutine: (id: Long) -> Unit,
  onAdd: () -> Unit,
  onDeleteRoutines: (List<Routine>) -> Unit,
  onSwitchSelection: (List<RoutineListScreenViewModel.RoutineModel>, Boolean) -> Unit
) {
  val itemsSelected by remember(routines) { mutableStateOf(routines.any { it.isSelected }) }
  var openDeleteDialog by remember { mutableStateOf(false) }

  if (openDeleteDialog) {
    DeleteDialog(
      title = stringResource(R.string.dialog_title_delete_routines),
      onDismiss = { openDeleteDialog = false },
      onConfirm = {
        openDeleteDialog = false
        onDeleteRoutines(routines.filter { it.isSelected }.map { it.routine })
      })
  }

  BackHandler(enabled = itemsSelected) {
    onSwitchSelection(routines, false)
  }

  Surface {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .imePadding()
    ) {
      if (itemsSelected) SelectionTopBar(
        selectionCount = routines.count { it.isSelected },
        onDeleteSelected = { openDeleteDialog = true })
      else UnselectionTopBar(
        filterWord = filterWord, onFilterChanged = onFilterChanged, onAdd = onAdd
      )
      LoadingScreen(enabled = isLoading) {
        LazyColumn(
          userScrollEnabled = true,
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier
            .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(8.dp))
        ) {
          items(routines) { model ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceVariant)
                .combinedClickable(onClick = {
                  if (!itemsSelected) onSelectRoutine(model.routine.id)
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
                Text(text = model.routine.name, fontWeight = FontWeight.Bold)
              }
              if (model.schedule != null) {
                Box(
                  contentAlignment = Alignment.TopEnd,
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                  ScheduleTrailing(schedule = model.schedule)
                }
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
        contentDescription = stringResource(R.string.cd_add_routine)
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
        contentDescription = stringResource(R.string.cd_delete_routine)
      )
    }
  })
}

@Composable
private fun ScheduleTrailing(schedule: RoutineSchedule) {
  fun dayIsSelected(day: Day, schedule: RoutineSchedule): Boolean {
    return when (day) {
      Day.SUNDAY -> schedule.sunday
      Day.MONDAY -> schedule.monday
      Day.TUESDAY -> schedule.tuesday
      Day.WEDNESDAY -> schedule.wednesday
      Day.THURSDAY -> schedule.thursday
      Day.FRIDAY -> schedule.friday
      Day.SATURDAY -> schedule.saturday
    }
  }

  val dayOrder = Calendar.getInstance().getLocalDayOrder()

  if (Day.entries.none { dayIsSelected(it, schedule) }) {
    Text(
      text = stringResource(R.string.label_inactive),
      color = MaterialTheme.colorScheme.outline,
      style = MaterialTheme.typography.labelSmall
    )
  }
  else if (Day.entries.all { dayIsSelected(it, schedule) }) {
    Text(
      text = stringResource(R.string.label_every_day),
      color = MaterialTheme.colorScheme.secondary,
      style = MaterialTheme.typography.labelSmall
    )
  }
  else {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      repeat(7) { i ->
        Text(
          text = stringResource(dayOrder[i].nameResourceKey).take(2),
          color = ifElse(
            condition = dayIsSelected(day = dayOrder[i], schedule = schedule),
            ifTrue = { MaterialTheme.colorScheme.secondary },
            ifFalse = { MaterialTheme.colorScheme.outline }),
          style = MaterialTheme.typography.labelSmall
        )
      }
    }
  }
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun Preview() {
  ExerciseTrackerTheme(darkTheme = true) {
    RoutineListScreen(
      routines = listOf(
      RoutineListScreenViewModel.RoutineModel(
        routine = Routine(id = 0, name = "Routine 1"),
        schedule = RoutineSchedule(id = 0, routineId = 0, monday = true),
        isSelected = true
      ), RoutineListScreenViewModel.RoutineModel(
        routine = Routine(id = 1, name = "Routine 2"),
        schedule = RoutineSchedule(id = 1, routineId = 0, monday = true),
        isSelected = false
      )
    ),
      filterWord = String.EMPTY,
      isLoading = false,
      onFilterChanged = {},
      onSelectRoutine = {},
      onAdd = {},
      onDeleteRoutines = {},
      onSwitchSelection = { _, _ -> })
  }
}