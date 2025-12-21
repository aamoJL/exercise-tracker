package com.aamo.exercisetracker.features.routine.view

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
import androidx.navigation.toRoute
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.RoutineDatabase
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.ExerciseWithProgress
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineWithExerciseProgresses
import com.aamo.exercisetracker.features.routine.view.use_cases.fetchRoutineFlow
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.components.inputs.BackNavigationIconButton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

@Serializable
data class RoutineScreen(val id: Long = 0L, val showProgress: Boolean = false)

class RoutineScreenViewModel(fetchData: () -> Flow<RoutineWithExerciseProgresses?>) : ViewModel() {
  val model = fetchData().stateIn(
    scope = viewModelScope, started = SharingStarted.Lazily, initialValue = null
  )
}

fun NavGraphBuilder.routineScreen(
  onBack: () -> Unit,
  onAddExercise: (routineId: Long) -> Unit,
  onSelectExercise: (id: Long) -> Unit,
  onEdit: (id: Long) -> Unit
) {
  composable<RoutineScreen> { navStack ->
    val (routineId: Long, showProgress: Boolean) = navStack.toRoute<RoutineScreen>()
    val dao = RoutineDatabase.getDatabase(LocalContext.current.applicationContext).routineDao()
    val viewmodel: RoutineScreenViewModel = viewModel(factory = viewModelFactory {
      initializer {
        RoutineScreenViewModel(fetchData = { fetchRoutineFlow(dao = dao, routineId = routineId) })
      }
    })
    val model by viewmodel.model.collectAsStateWithLifecycle()

    LoadingScreen(model = model) {
      RoutineScreenContent(
        routine = it.routine,
        exercises = it.exerciseProgresses,
        showFinishedIcon = showProgress,
        onBack = onBack,
        onAddExercise = onAddExercise,
        onSelectExercise = onSelectExercise,
        onEdit = { onEdit(routineId) })
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RoutineScreenContent(
  routine: Routine,
  exercises: List<ExerciseWithProgress>,
  showFinishedIcon: Boolean,
  onBack: () -> Unit,
  onAddExercise: (routineId: Long) -> Unit,
  onSelectExercise: (id: Long) -> Unit,
  onEdit: () -> Unit
) {
  Scaffold(topBar = {
    TopAppBar(title = { Text(routine.name) }, navigationIcon = {
      BackNavigationIconButton(onBack = onBack)
    }, actions = {
      IconButton(onClick = onEdit) {
        Icon(
          painter = painterResource(R.drawable.rounded_edit_24),
          contentDescription = stringResource(R.string.cd_edit_routine)
        )
      }
      IconButton(onClick = { onAddExercise(routine.id) }) {
        Icon(
          painter = painterResource(R.drawable.rounded_add_24),
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
        items(exercises, key = { it.exercise.id }) { exercise ->
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
              Text(text = exercise.exercise.name, fontWeight = FontWeight.Bold)

              if (showFinishedIcon && exercise.progress != null) {
                val finished =
                  System.currentTimeMillis().milliseconds.inWholeDays <= exercise.progress.finishedDate.time.milliseconds.inWholeDays

                if (finished) {
                  Icon(
                    painter = painterResource(R.drawable.round_done_24),
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
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun Preview() {
  RoutineScreenContent(
    routine = Routine(id = 1L, name = "Routine 1"), exercises = listOf(
    ExerciseWithProgress(
      exercise = Exercise(id = 1L, routineId = 1L, name = "Exercise 1"),
      progress = ExerciseProgress(exerciseId = 1L, finishedDate = Date())
    ),
    ExerciseWithProgress(
      exercise = Exercise(id = 2L, routineId = 1L, name = "Exercise 1"),
      progress = ExerciseProgress(
        exerciseId = 2L,
        finishedDate = Date(System.currentTimeMillis() - 1.days.inWholeMilliseconds)
      )
    ),
    ExerciseWithProgress(
      exercise = Exercise(id = 3L, routineId = 1L, name = "Exercise 1"), progress = null
    ),
  ), showFinishedIcon = true, onBack = {}, onAddExercise = {}, onSelectExercise = {}, onEdit = {})
}