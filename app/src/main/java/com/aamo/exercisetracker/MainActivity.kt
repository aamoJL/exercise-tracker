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
import com.aamo.exercisetracker.features.dailies.dailiesPage
import com.aamo.exercisetracker.features.dailies.toDailiesPage
import com.aamo.exercisetracker.features.home.HomePage
import com.aamo.exercisetracker.features.home.homePage
import com.aamo.exercisetracker.features.routine.routineListPage
import com.aamo.exercisetracker.features.routine.routinePage
import com.aamo.exercisetracker.features.routine.toRoutinePage
import com.aamo.exercisetracker.features.routine.toRoutinesPage
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import java.util.Calendar

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
            homePage(
              onSelectDaily = {
              navController.toDailiesPage(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
            },
              onSelectWeekly = { navController.toRoutinesPage() },
              onSelectMonthly = { /* TODO: monthly click command */ })
            dailiesPage()
            routineListPage(
              navController = navController,
              onBack = { navController.navigateUp() },
              onSelectRoutine = { id ->
                navController.toRoutinePage(id)
              })
            routinePage(
              navController = navController,
              onBack = { navController.navigateUp() },
              onSelectExercise = {
                /* TODO: exercise select command */
              })
          }
        }
      }
    }
  }
}