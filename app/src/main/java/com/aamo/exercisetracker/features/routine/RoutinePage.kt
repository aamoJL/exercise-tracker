package com.aamo.exercisetracker.features.routine

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.aamo.exercisetracker.features.exercise.AddExerciseFormScreen
import com.aamo.exercisetracker.features.exercise.addExerciseFormScreen
import com.aamo.exercisetracker.utility.extensions.general.onFalse
import kotlinx.serialization.Serializable

@Serializable
data class RoutinePage(val id: Long, val showProgress: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.routinePage(
  navController: NavController, onBack: () -> Unit, onSelectExercise: (Long) -> Unit
) {
  navigation<RoutinePage>(startDestination = RoutineScreen()) {
    routineScreen(
      onBack = onBack,
      onAddExercise = { routineId ->
        navController.navigate(AddExerciseFormScreen(routineId = routineId))
      },
      onSelectExercise = onSelectExercise,
      onEdit = { id -> navController.navigate(RoutineFormScreen(id = id)) },
    )
    routineFormScreen(onBack = onBack, onSaved = { id ->
      navController.navigate(RoutineScreen(id)) {
        launchSingleTop = true
        popUpTo<RoutineFormScreen> {
          inclusive = true
        }
      }
    }, onDeleted = {
      navController.popBackStack<RoutinePage>(inclusive = true)
    })
    addExerciseFormScreen(onBack = onBack, onSaved = { id ->
      navController.popBackStack<RoutineScreen>(inclusive = false).onFalse {
        navController.navigateUp()
      }
    })
  }
}