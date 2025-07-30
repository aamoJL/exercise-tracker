package com.aamo.exercisetracker.features.monthlyProgress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.components.SearchTextField
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object MonthlyProgressListScreen

class MonthlyProgressListScreenViewModel(fetchData: () -> Flow<List<String>>) : ViewModel() {
  var isLoading by mutableStateOf(true)
    private set

  private val _progresses = fetchData().map { list -> list.sortedBy { it } }.also {
    viewModelScope.launch {
      it.collect { isLoading = false }
    }
  }

  private var _filterWord = MutableStateFlow(String.EMPTY)
  val filterWord = _filterWord.asStateFlow()

  val filteredProgresses = combine(_progresses, filterWord) { progress, word ->
    progress.filter { it.contains(word, ignoreCase = true) }
  }.stateIn(scope = viewModelScope, started = SharingStarted.Lazily, initialValue = emptyList())

  fun setFilterWord(word: String) {
    _filterWord.update { word }
  }
}

fun NavGraphBuilder.monthlyProgressListScreen(
  onSelectProgress: (id: Long) -> Unit, onAddProgress: () -> Unit
) {
  composable<MonthlyProgressListScreen> {
    //val context = LocalContext.current.applicationContext
    val viewmodel: MonthlyProgressListScreenViewModel = viewModel(factory = viewModelFactory {
      initializer {
        MonthlyProgressListScreenViewModel(fetchData = {
          // TODO: fetch monthly progresses
          flow {
            @Suppress("HardCodedStringLiteral") emit(listOf("Test Progress 1", "Test Progress 2"))
          }
        })
      }
    })
    val progresses by viewmodel.filteredProgresses.collectAsStateWithLifecycle()
    val filterWord by viewmodel.filterWord.collectAsStateWithLifecycle()

    MonthlyProgressListScreen(
      progresses = progresses,
      filterWord = filterWord,
      isLoading = viewmodel.isLoading,
      onFilterChanged = { viewmodel.setFilterWord(it) },
      onSelectProgress = onSelectProgress,
      onAdd = onAddProgress
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyProgressListScreen(
  progresses: List<String>,
  filterWord: String,
  isLoading: Boolean,
  onFilterChanged: (String) -> Unit,
  onSelectProgress: (id: Long) -> Unit,
  onAdd: () -> Unit
) {
  Surface {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .imePadding()
    ) {
      TopAppBar(title = { null }, actions = {
        SearchTextField(
          value = filterWord, onValueChange = onFilterChanged, modifier = Modifier.height(50.dp)
        )
        IconButton(onClick = onAdd) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(R.string.cd_add_monthly_progress)
          )
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
          items(progresses) { progress ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onSelectProgress(0L /* TODO: change to id */) }) {
              Text(
                text = progress,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 28.dp)
              )
//              if (schedule != null) {
//                Box(
//                  contentAlignment = Alignment.TopEnd,
//                  modifier = Modifier
//                    .fillMaxSize()
//                    .padding(horizontal = 12.dp, vertical = 8.dp)
//                ) {
//                  ScheduleTrailing(schedule = schedule)
//                }
//              }
            }
          }
        }
      }
    }
  }
}

//@Composable
//private fun ScheduleTrailing(schedule: RoutineSchedule) {
//  fun dayIsSelected(day: Day, schedule: RoutineSchedule): Boolean {
//    return when (day) {
//      Day.SUNDAY -> schedule.sunday
//      Day.MONDAY -> schedule.monday
//      Day.TUESDAY -> schedule.tuesday
//      Day.WEDNESDAY -> schedule.wednesday
//      Day.THURSDAY -> schedule.thursday
//      Day.FRIDAY -> schedule.friday
//      Day.SATURDAY -> schedule.saturday
//    }
//  }
//
//  val dayOrder = Calendar.getInstance().getLocalDayOrder()
//
//  if (Day.entries.none { dayIsSelected(it, schedule) }) {
//    Text(
//      text = stringResource(R.string.label_inactive),
//      color = MaterialTheme.colorScheme.outline,
//      style = MaterialTheme.typography.labelSmall
//    )
//  }
//  else if (Day.entries.all { dayIsSelected(it, schedule) }) {
//    Text(
//      text = stringResource(R.string.label_every_day),
//      color = MaterialTheme.colorScheme.secondary,
//      style = MaterialTheme.typography.labelSmall
//    )
//  }
//  else {
//    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//      repeat(7) { i ->
//        Text(
//          text = stringResource(dayOrder[i].nameResourceKey).take(2),
//          color = ifElse(
//            condition = dayIsSelected(day = dayOrder[i], schedule = schedule),
//            ifTrue = { MaterialTheme.colorScheme.secondary },
//            ifFalse = { MaterialTheme.colorScheme.outline }),
//          style = MaterialTheme.typography.labelSmall
//        )
//      }
//    }
//  }
//}