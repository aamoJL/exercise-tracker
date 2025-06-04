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
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aamo.exercisetracker.features.dailies.DailiesScreen
import com.aamo.exercisetracker.features.home.HomeScreen
import com.aamo.exercisetracker.features.routines.WeeklyRoutinesScreen
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOfWeek
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import java.util.Calendar

/* Enum class for screen navigation
 */
enum class Screen(private val route: String, val argumentName: String = "") {
  HOME("Home"),
  DAILIES("Dailies"),
  ROUTINES("Routines");

  fun getRoute(): String = when (argumentName) {
    String.EMPTY -> route
    else -> route.plus("{$argumentName}")
  }

  fun getRouteWithArgument(arg: String): String = route.plus(arg)
}

class MainActivity : ComponentActivity() {
  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val navController = rememberNavController()

      ExerciseTrackerTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          NavHost(
            navController = navController, startDestination = Screen.HOME.getRoute()
          ) {
            composable(route = Screen.HOME.getRoute()) {
              HomeScreen(
                onTodayClick = { navController.navigate(Screen.DAILIES.getRoute()) },
                onWeeklyClick = { navController.navigate(Screen.ROUTINES.getRoute()) })
            }
            composable(route = Screen.DAILIES.getRoute()) { navBackStackEntry ->
              DailiesScreen(dayNumber = Calendar.getInstance().getLocalDayOfWeek())
            }
            composable(route = Screen.ROUTINES.getRoute()) {
              WeeklyRoutinesScreen()
            }
          }
        }
      }
    }
  }
}