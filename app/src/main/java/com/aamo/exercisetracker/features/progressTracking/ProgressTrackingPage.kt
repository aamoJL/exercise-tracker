package com.aamo.exercisetracker.features.progressTracking

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import kotlinx.serialization.Serializable

@Serializable
data class ProgressTrackingPage(val progressId: Long)

fun NavGraphBuilder.progressTrackingPage(navController: NavController) {
  navigation<ProgressTrackingPage>(startDestination = ProgressTrackingScreen(progressId = 0L)) {
    progressTrackingScreen()
    trackedProgressFormScreen(
      onBack = { navController.navigateUp() },
      onSaved = { _ -> navController.navigateUp() },
      onDeleted = { navController.popBackStack<ProgressTrackingPage>(inclusive = true) })
  }
}