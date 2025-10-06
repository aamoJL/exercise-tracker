package com.aamo.exercisetracker.features.exercise

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.aamo.exercisetracker.features.routine.RoutineScreen
import com.aamo.exercisetracker.utility.extensions.general.onFalse

fun NavGraphBuilder.exercisePage(navController: NavController) {
  exerciseScreen(onBack = { navController.navigateUp() }, onEdit = { exerciseId, routineId ->
    navController.navigate(
      ExerciseFormScreen(exerciseId = exerciseId, routineId = routineId)
    )
  })
  exerciseFormScreen(onBack = { navController.navigateUp() }, onAdd = { id ->
    navController.popBackStack<RoutineScreen>(inclusive = false).onFalse {
      navController.navigateUp()
    }
  }, onUpdate = { id ->
    navController.navigate(ExerciseScreen(id)) {
      launchSingleTop = true
      popUpTo<ExerciseScreen> { inclusive = true }
    }
  }, onDeleted = {
    navController.popBackStack<ExerciseScreen>(inclusive = true).onFalse {
      navController.navigateUp()
    }
  })
}