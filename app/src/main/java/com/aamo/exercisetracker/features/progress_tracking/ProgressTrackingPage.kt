package com.aamo.exercisetracker.features.progress_tracking

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import kotlinx.serialization.Serializable

@Serializable
object ProgressTrackingPage

fun NavGraphBuilder.progressTrackingPage(navController: NavController) {
  navigation<ProgressTrackingPage>(startDestination = ProgressTrackingScreen(progressId = 0L)) {
    progressTrackingScreen(onBack = { navController.navigateUp() }, onEdit = { id ->
      navController.navigate(TrackedProgressFormScreen(progressId = id))
    }, onShowRecords = { id ->
      navController.navigate(TrackedProgressRecordListScreen(progressId = id))
    })
    trackedProgressFormScreen(onBack = { navController.navigateUp() }, onSaved = { id ->
      if (navController.popBackStack<ProgressTrackingScreen>(inclusive = true)) {
        navController.navigate(ProgressTrackingScreen(progressId = id)) { launchSingleTop = true }
      }
      else {
        navController.navigateUp()
      }
    }, onDeleted = { navController.popBackStack<ProgressTrackingPage>(inclusive = true) })
    trackedProgressRecordListScreen(
      onBack = { navController.navigateUp() })
  }
}