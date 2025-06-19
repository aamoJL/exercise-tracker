package com.aamo.exercisetracker.features.routine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.aamo.exercisetracker.database.RoutineDatabase
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineWithExercises
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class RoutineScreen(val id: Long = 0L)

fun NavGraphBuilder.routineScreen(
  onBack: () -> Unit,
  onAddExercise: (routineId: Long) -> Unit,
  onSelectExercise: (id: Long) -> Unit,
  onEdit: (id: Long) -> Unit
) {
  composable<RoutineScreen> { navStack ->
    val (id: Long) = navStack.toRoute<RoutineScreen>()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current.applicationContext
    var routine by remember {
      mutableStateOf(
        RoutineWithExercises(routine = Routine(name = String.EMPTY), exercises = emptyList())
      )
    }

    LaunchedEffect(true) {
      scope.launch {
        runCatching {
          checkNotNull(
            RoutineDatabase.getDatabase(context).routineDao().getRoutineWithExercises(id)
          )
        }.onSuccess { dbRoutine -> routine = dbRoutine }.onFailure { onBack() }
      }
    }

    RoutineScreen(
      routine = routine,
      onBack = onBack,
      onAddExercise = onAddExercise,
      onSelectExercise = onSelectExercise,
      onEdit = { onEdit(id) })
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RoutineScreen(
  routine: RoutineWithExercises,
  onBack: () -> Unit,
  onAddExercise: (routineId: Long) -> Unit,
  onSelectExercise: (id: Long) -> Unit,
  onEdit: () -> Unit
) {
  val (routine, exercises) = routine

  Scaffold(topBar = {
    TopAppBar(title = { Text(routine.name) }, navigationIcon = {
      BackNavigationIconButton(onBack = onBack)
    }, actions = {
      IconButton(onClick = onEdit) {
        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit routine")
      }
      IconButton(onClick = { onAddExercise(routine.id) }) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add exercise")
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
        items(exercises) { exercise ->
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(color = MaterialTheme.colorScheme.surfaceVariant)
              .clickable { onSelectExercise(exercise.id) }) {
            Text(
              text = exercise.name,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 28.dp)
            )
          }
        }
      }
    }
  }
}