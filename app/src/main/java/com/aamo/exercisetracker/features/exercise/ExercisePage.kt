package com.aamo.exercisetracker.features.exercise

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import kotlinx.serialization.Serializable

@Serializable
data class ExercisePage(val id: Long)

fun NavGraphBuilder.exercisePage(navController: NavController) {
  navigation<ExercisePage>(startDestination = ExerciseScreen()) {
    exerciseScreen(onBack = { navController.navigateUp() }, onEdit = { id ->
      navController.navigate(EditExerciseFormScreen(id))
    })
    editExerciseFormScreen(onBack = { navController.navigateUp() }, onSaved = { id ->
      navController.navigate(ExerciseScreen(id)) {
        launchSingleTop = true
        popUpTo<ExerciseScreen> { inclusive = true }
      }
    }, onDeleted = {
      navController.popBackStack<ExercisePage>(inclusive = true)
    })
  }
}