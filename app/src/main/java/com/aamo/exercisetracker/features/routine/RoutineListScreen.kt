package com.aamo.exercisetracker.features.routine

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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
import com.aamo.exercisetracker.database.RoutineDatabase
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.components.SearchTextField
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOrder
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Calendar

@Serializable
object RoutineListScreen

class RoutineListScreenViewModel(context: Context) : ViewModel() {
  private val database: RoutineDatabase = RoutineDatabase.getDatabase(context)

  private val _routines = MutableStateFlow<List<RoutineWithSchedule>>(emptyList())
  val routines = _routines.asStateFlow()

  var isLoading by mutableStateOf(true)
    private set

  init {
    viewModelScope.launch {
      database.routineDao().getRoutinesWithScheduleFlow()
        .map { list -> list.sortedBy { it.routine.name } }.collect {
          _routines.value = it
          isLoading = false
        }
    }
  }
}

fun NavGraphBuilder.routineListScreen(
  onSelectRoutine: (id: Long) -> Unit, onAddRoutine: () -> Unit
) {
  composable<RoutineListScreen> {
    val context = LocalContext.current.applicationContext
    val viewmodel: RoutineListScreenViewModel = viewModel(factory = viewModelFactory {
      initializer { RoutineListScreenViewModel(context = context) }
    })
    val routines by viewmodel.routines.collectAsStateWithLifecycle()

    RoutineListScreen(
      routines = routines,
      isLoading = viewmodel.isLoading,
      onSelectRoutine = onSelectRoutine,
      onAdd = onAddRoutine
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineListScreen(
  routines: List<RoutineWithSchedule>,
  isLoading: Boolean,
  onSelectRoutine: (id: Long) -> Unit,
  onAdd: () -> Unit
) {
  Surface {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .imePadding()
    ) {
      TopAppBar(title = { Text(String.EMPTY) }, actions = {
        SearchTextField(
          value = String.EMPTY,
          onValueChange = { /* TODO: value change command */ },
          modifier = Modifier.height(50.dp)
        )
        IconButton(onClick = onAdd) {
          Icon(imageVector = Icons.Filled.Add, contentDescription = "Add routine")
        }
      })
      LoadingScreen(enabled = isLoading) {
        LazyColumn(
          userScrollEnabled = true,
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier
            .padding(top = 4.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(8.dp))
        ) {
          items(routines) { (routine, schedule) ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onSelectRoutine(routine.id) }) {
              Text(
                text = routine.name,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 28.dp)
              )
              if (schedule != null) {
                Box(
                  contentAlignment = Alignment.TopEnd,
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                  ScheduleTrailing(schedule = schedule)
                }
              }
            }
          }
        }
      }
    }
  }
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
      text = "Inactive",
      color = MaterialTheme.colorScheme.outline,
      style = MaterialTheme.typography.labelSmall
    )
  }
  else if (Day.entries.all { dayIsSelected(it, schedule) }) {
    Text(
      text = "Every day",
      color = MaterialTheme.colorScheme.secondary,
      style = MaterialTheme.typography.labelSmall
    )
  }
  else {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      repeat(7) { i ->
        Text(
          stringResource(dayOrder[i].nameResourceKey).take(2), color = if (dayIsSelected(
              day = dayOrder[i], schedule = schedule
            )) MaterialTheme.colorScheme.secondary
          else MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall
        )
      }
    }
  }
}