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
import com.aamo.exercisetracker.features.dailies.Dailies
import com.aamo.exercisetracker.features.dailies.dailiesDestination
import com.aamo.exercisetracker.features.home.Home
import com.aamo.exercisetracker.features.home.homeDestination
import com.aamo.exercisetracker.features.routines.EditRoutine
import com.aamo.exercisetracker.features.routines.Routines
import com.aamo.exercisetracker.features.routines.editRoutineDestination
import com.aamo.exercisetracker.features.routines.routinesDestination
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOfWeek
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
          NavHost(navController = navController, startDestination = Home) {
            homeDestination(
              onTodayClick = {
              navController.navigate(
                route = Dailies(Calendar.getInstance().getLocalDayOfWeek().getDayNumber())
              )
            },
              onWeeklyClick = { navController.navigate(route = Routines) },
              onMonthlyClick = { /* TODO: monthly click command */ })
            dailiesDestination()
            routinesDestination(
              onAddOrEdit = { navController.navigate(route = EditRoutine(0)) },
              onBack = { navController.popBackStack() })
            editRoutineDestination(
              onBack = { navController.popBackStack() },
              onSave = { /* TODO: save routine command */ })
          }
        }
      }
    }
  }
}