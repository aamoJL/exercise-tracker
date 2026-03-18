package com.aamo.exercisetracker.features.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.aamo.exercisetracker.features.exercise.exercisePage
import com.aamo.exercisetracker.features.exercise.view.ExerciseScreen
import com.aamo.exercisetracker.features.progress_tracking.progressTrackingPage
import com.aamo.exercisetracker.features.routine.routinePage
import com.aamo.exercisetracker.ui.components.BackgroundSurface

@Composable
fun HomePage() {
  val navController = rememberNavController()

  BackgroundSurface(modifier = Modifier.fillMaxSize()) {
    NavHost(navController = navController, startDestination = HomeScreen) {
      homeScreen(navController = navController)
      routinePage(
        navController = navController,
        onBack = { navController.navigateUp() },
        onSelectExercise = { id ->
          navController.navigate(ExerciseScreen(id = id)) { launchSingleTop = true }
        })
      exercisePage(navController = navController)
      progressTrackingPage(navController = navController)
    }
  }
}