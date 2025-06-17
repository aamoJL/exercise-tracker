package com.aamo.exercisetracker.features.routine

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.aamo.exercisetracker.features.exercise.toExerciseFormScreen
import kotlinx.serialization.Serializable

@Serializable
data class RoutinePage(val id: Long)

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.routinePage(
  navController: NavController, onBack: () -> Unit, onSelectExercise: (Int) -> Unit
) {
  navigation<RoutinePage>(startDestination = RoutineScreen()) {
    routineScreen(
      onBack = onBack,
      onAddExercise = { navController.toExerciseFormScreen(0) },
      onSelectExercise = onSelectExercise,
      onEdit = { id -> navController.navigate(RoutineFormScreen(id)) },
    )
    routineFormScreen(onBack = onBack, onSuccess = { id ->
      navController.navigate(RoutineScreen(id)) {
        launchSingleTop = true
        popUpTo<RoutineFormScreen> {
          inclusive = true
        }
      }
    })
  }
}