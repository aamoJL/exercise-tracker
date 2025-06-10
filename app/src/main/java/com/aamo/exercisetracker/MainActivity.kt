package com.aamo.exercisetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.aamo.exercisetracker.features.exercise.ExercisePage
import com.aamo.exercisetracker.features.exercise.exercisePage
import com.aamo.exercisetracker.features.home.HomePage
import com.aamo.exercisetracker.features.home.homePage
import com.aamo.exercisetracker.features.routine.routinePage
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme

class MainActivity : ComponentActivity() {
  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val navController = rememberNavController()

      ExerciseTrackerTheme {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
          NavHost(navController = navController, startDestination = HomePage) {
            homePage(navController = navController)
            routinePage(
              navController = navController,
              onBack = { navController.navigateUp() },
              onSelectExercise = { id -> navController.navigate(ExercisePage(id = id)) })
            exercisePage(navController = navController, onBack = { navController.navigateUp() })
          }
        }
      }
    }
  }
}