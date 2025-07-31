package com.aamo.exercisetracker.features.progressTracking

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
data class TrackedProgressFormScreen(val progressId: Long)

fun NavGraphBuilder.trackedProgressFormScreen() {
  composable<TrackedProgressFormScreen> { navStack ->
    val progressId = navStack.toRoute<TrackedProgressFormScreen>().progressId
    // TODO: progress form
  }
}