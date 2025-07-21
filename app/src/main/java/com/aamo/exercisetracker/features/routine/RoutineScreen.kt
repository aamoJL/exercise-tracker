package com.aamo.exercisetracker.features.routine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.toRoute
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.RoutineDatabase
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseWithProgress
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineWithExerciseProgresses
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.ZoneId

@Serializable
data class RoutineScreen(val id: Long = 0L, val showProgress: Boolean = false)

class RoutineScreenViewModel(fetchData: () -> Flow<RoutineWithExerciseProgresses?>) : ViewModel() {
  private val _exercises = MutableStateFlow<List<ExerciseWithProgress>>(emptyList())
  val exercises = _exercises.asStateFlow()

  var routine: Routine? by mutableStateOf(null)
    private set
  var isLoading by mutableStateOf(true)
    private set

  init {
    viewModelScope.launch {
      fetchData().collect { item ->
        isLoading = false
        item?.also {
          routine = item.routine
          _exercises.update { item.exerciseProgresses }
        }
      }
    }
  }
}

fun NavGraphBuilder.routineScreen(
  onBack: () -> Unit,
  onAddExercise: (routineId: Long) -> Unit,
  onSelectExercise: (id: Long) -> Unit,
  onEdit: (id: Long) -> Unit
) {
  composable<RoutineScreen> { navStack ->
    val (routineId: Long, showProgress: Boolean) = navStack.toRoute<RoutineScreen>()
    val context = LocalContext.current.applicationContext
    val viewmodel: RoutineScreenViewModel = viewModel(factory = viewModelFactory {
      initializer {
        RoutineScreenViewModel(
          fetchData = {
            RoutineDatabase.getDatabase(context).routineDao()
              .getRoutineWithProgressesFlow(routineId)
          },
        )
      }
    })
    val routine = viewmodel.routine
    val exercises by viewmodel.exercises.collectAsStateWithLifecycle()

    LoadingScreen(enabled = viewmodel.isLoading) {
      if (routine != null) {
        RoutineScreen(
          routine = routine,
          exercises = exercises,
          showProgress = showProgress,
          onBack = onBack,
          onAddExercise = onAddExercise,
          onSelectExercise = onSelectExercise,
          onEdit = { onEdit(routineId) })
      }
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RoutineScreen(
  routine: Routine,
  exercises: List<ExerciseWithProgress>,
  showProgress: Boolean,
  onBack: () -> Unit,
  onAddExercise: (routineId: Long) -> Unit,
  onSelectExercise: (id: Long) -> Unit,
  onEdit: () -> Unit
) {
  val dateMillis = remember {
    LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
  }
  val sortedExercises = remember(exercises) {
    exercises.map { (exercise, progress) ->
      object {
        val exercise: Exercise = exercise
        val isFinished: Boolean? = ifElse(
          condition = showProgress,
          ifTrue = { progress?.finishedDate?.time?.compareTo(dateMillis)?.let { it > 0 } == true },
          ifFalse = { null })
      }
    }.sortedBy { it.exercise.name }
  }

  Scaffold(topBar = {
    TopAppBar(title = { Text(routine.name) }, navigationIcon = {
      BackNavigationIconButton(onBack = onBack)
    }, actions = {
      IconButton(onClick = onEdit) {
        Icon(
          imageVector = Icons.Filled.Edit,
          contentDescription = stringResource(R.string.cd_edit_routine)
        )
      }
      IconButton(onClick = { onAddExercise(routine.id) }) {
        Icon(
          imageVector = Icons.Filled.Add,
          contentDescription = stringResource(R.string.cd_add_exercise)
        )
      }
    })
  }) { innerPadding ->
    Surface(modifier = Modifier.padding(innerPadding)) {
      LazyColumn(
        userScrollEnabled = true,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
          .padding(8.dp)
          .clip(RoundedCornerShape(8.dp))
      ) {
        items(sortedExercises, key = { it.exercise.id }) { exercise ->
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(color = MaterialTheme.colorScheme.surfaceVariant)
              .clickable { onSelectExercise(exercise.exercise.id) }) {
            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 28.dp)
            ) {
              Text(
                text = exercise.exercise.name,
                fontWeight = FontWeight.Bold,
              )
              if (exercise.isFinished == true) {
                Icon(
                  imageVector = Icons.Filled.Done,
                  contentDescription = stringResource(R.string.cd_done)
                )
              }
            }
          }
        }
      }
    }
  }
}