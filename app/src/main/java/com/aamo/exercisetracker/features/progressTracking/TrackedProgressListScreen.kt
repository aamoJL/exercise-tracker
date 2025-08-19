package com.aamo.exercisetracker.features.progressTracking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.RoutineDatabase
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.components.SearchTextField
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object TrackedProgressListScreen

class TrackedProgressListScreenViewModel(fetchData: () -> Flow<List<TrackedProgress>>) :
        ViewModel() {
  var isLoading by mutableStateOf(true)
    private set

  private val _progresses = fetchData().map { list -> list.sortedBy { it.name } }.also {
    viewModelScope.launch {
      it.collect { isLoading = false }
    }
  }

  private var _filterWord = MutableStateFlow(String.EMPTY)
  val filterWord = _filterWord.asStateFlow()

  val filteredProgresses = combine(_progresses, filterWord) { progress, word ->
    progress.filter { it.name.contains(word, ignoreCase = true) }
  }.stateIn(scope = viewModelScope, started = SharingStarted.Lazily, initialValue = emptyList())

  fun setFilterWord(word: String) {
    _filterWord.update { word }
  }
}

fun NavGraphBuilder.trackedProgressListScreen(
  onSelectProgress: (id: Long) -> Unit, onAddProgress: () -> Unit
) {
  composable<TrackedProgressListScreen> {
    val context = LocalContext.current.applicationContext
    val viewmodel: TrackedProgressListScreenViewModel = viewModel(factory = viewModelFactory {
      initializer {
        TrackedProgressListScreenViewModel(fetchData = {
          RoutineDatabase.getDatabase(context).trackedProgressDao().getProgressesFlow()
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
      onAdd = onAddProgress
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackedProgressListScreen(
  progresses: List<TrackedProgress>,
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
        SearchTextField(value = filterWord, onValueChange = onFilterChanged)
        IconButton(onClick = onAdd) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(R.string.cd_add_tracked_progress)
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
                .clickable { onSelectProgress(progress.id) }) {
              Text(
                text = progress.name,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 28.dp)
              )
              Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier
                  .fillMaxSize()
                  .padding(horizontal = 12.dp, vertical = 8.dp)
              ) {
                IntervalTrailing(intervalWeeks = progress.intervalWeeks)
              }
            }
          }
        }
      }
    }
  }
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