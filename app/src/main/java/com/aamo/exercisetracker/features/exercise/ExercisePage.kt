package com.aamo.exercisetracker.features.exercise

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import kotlinx.serialization.Serializable

@Serializable data class ExercisePage(val id: Int)

fun NavGraphBuilder.exercisePage(navController: NavController, onBack: () -> Unit) {
  navigation<ExercisePage>(startDestination = ExerciseScreen()) {
    exerciseScreen(onBack = onBack, onEdit = { id ->
      navController.toExerciseFormScreen(id)
    })
    exerciseFormScreen(onBack = onBack, onSave = {
      /* TODO: Exercise save command */
    })
  }
}