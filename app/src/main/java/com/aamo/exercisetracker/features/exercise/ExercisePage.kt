package com.aamo.exercisetracker.features.exercise

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import kotlinx.serialization.Serializable

@Serializable data class ExercisePage(val id: Int)

fun NavGraphBuilder.exercisePage(onBack: () -> Unit) {
  navigation<ExercisePage>(startDestination = ExerciseScreen()) {
    exerciseScreen(onBack = onBack)
  }
}

fun NavController.toExercisePage(id: Int) {
  this.navigate(ExercisePage(id = id))
}