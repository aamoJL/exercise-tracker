package com.aamo.exercisetracker.features.progressTracking

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import kotlinx.serialization.Serializable

@Serializable
data class ProgressTrackingPage(val progressId: Long)

fun NavGraphBuilder.progressTrackingPage() {
  navigation<ProgressTrackingPage>(startDestination = ProgressTrackingScreen(progressId = 0L)) {
    progressTrackingScreen()
    trackedProgressFormScreen()
  }
}