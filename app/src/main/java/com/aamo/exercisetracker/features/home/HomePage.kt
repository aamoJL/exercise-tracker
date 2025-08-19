package com.aamo.exercisetracker.features.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.aamo.exercisetracker.features.exercise.ExercisePage
import com.aamo.exercisetracker.features.exercise.exercisePage
import com.aamo.exercisetracker.features.progressTracking.progressTrackingPage
import com.aamo.exercisetracker.features.routine.routinePage

@Composable
fun HomePage() {
  val navController = rememberNavController()

  Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
    NavHost(navController = navController, startDestination = HomeScreen) {
      homeScreen(navController = navController)
      routinePage(
        navController = navController,
        onBack = { navController.navigateUp() },
        onSelectExercise = { id ->
          navController.navigate(ExercisePage(id = id)) {
            launchSingleTop = true
          }
        })
      exercisePage(navController = navController)
      progressTrackingPage(navController = navController)
    }
  }
}