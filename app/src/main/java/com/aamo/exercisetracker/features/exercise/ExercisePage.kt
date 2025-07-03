package com.aamo.exercisetracker.features.exercise

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import kotlinx.serialization.Serializable

@Serializable
data class ExercisePage(val id: Long)

fun NavGraphBuilder.exercisePage(navController: NavController, onBack: () -> Unit) {
  navigation<ExercisePage>(startDestination = ExerciseScreen()) {
    exerciseScreen(onBack = onBack, onEdit = { id ->
      navController.navigate(EditExerciseFormScreen(id))
    })
    editExerciseFormScreen(onBack = onBack, onSaved = { id ->
      navController.navigate(ExerciseScreen(id)) {
        launchSingleTop = true
        popUpTo<ExerciseScreen> { inclusive = true }
      }
    }, onDeleted = {
      navController.popBackStack<ExercisePage>(inclusive = true)
    })
  }
}