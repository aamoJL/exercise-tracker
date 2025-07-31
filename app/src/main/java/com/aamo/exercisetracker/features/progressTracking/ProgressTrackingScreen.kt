package com.aamo.exercisetracker.features.progressTracking

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
data class ProgressTrackingScreen(val progressId: Long)

fun NavGraphBuilder.progressTrackingScreen() {
  composable<ProgressTrackingScreen> { navStack ->
    val progressId = navStack.toRoute<ProgressTrackingScreen>().progressId
    // TODO: progress screen
  }
}