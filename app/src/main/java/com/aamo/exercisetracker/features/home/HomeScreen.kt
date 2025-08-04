package com.aamo.exercisetracker.features.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.features.dailies.DailiesScreen
import com.aamo.exercisetracker.features.dailies.dailiesScreen
import com.aamo.exercisetracker.features.progressTracking.ProgressTrackingScreen
import com.aamo.exercisetracker.features.progressTracking.TrackedProgressFormScreen
import com.aamo.exercisetracker.features.progressTracking.TrackedProgressListScreen
import com.aamo.exercisetracker.features.progressTracking.trackedProgressListScreen
import com.aamo.exercisetracker.features.routine.RoutineFormScreen
import com.aamo.exercisetracker.features.routine.RoutineListScreen
import com.aamo.exercisetracker.features.routine.RoutinePage
import com.aamo.exercisetracker.features.routine.routineListScreen
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.general.onFalse
import com.aamo.exercisetracker.utility.extensions.navigation.destinationEquals
import kotlinx.serialization.Serializable

@Serializable
object HomeScreen

fun NavGraphBuilder.homeScreen(navController: NavController) {
  composable<HomeScreen> {
    HomeScreen(mainNavController = navController)
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(mainNavController: NavController) {
  val homeNavController = rememberNavController()
  val navStackEntry = homeNavController.currentBackStackEntryAsState().value

  Column {
    NavHost(
      navController = homeNavController,
      startDestination = DailiesScreen(Day.today()),
      modifier = Modifier.weight(1f)
    ) {
      dailiesScreen(onSelectRoutine = { id ->
        mainNavController.navigate(RoutinePage(id = id, showProgress = true)) {
          launchSingleTop = true
        }
      })
      routineListScreen(onSelectRoutine = { id ->
        mainNavController.navigate(RoutinePage(id = id)) {
          launchSingleTop = true
        }
      }, onAddRoutine = {
        mainNavController.navigate(RoutineFormScreen(id = 0L)) {
          launchSingleTop = true
        }
      })
      trackedProgressListScreen(onSelectProgress = { id ->
        mainNavController.navigate(ProgressTrackingScreen(progressId = id)) {
          launchSingleTop = true
        }
      }, onAddProgress = {
        mainNavController.navigate(TrackedProgressFormScreen(progressId = 0L)) {
          launchSingleTop = true
        }
      })
    }

    // prevents bottom bar to be visible when IME is visible
    if (!WindowInsets.isImeVisible) {
      BottomAppBar {
        NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
          NavigationBarItem(
            selected = navStackEntry.destinationEquals(DailiesScreen::class),
            onClick = {
              navStackEntry.destinationEquals(DailiesScreen::class).onFalse {
                homeNavController.popBackStack(route = DailiesScreen::class, inclusive = false)
                  .onFalse { homeNavController.navigate(route = DailiesScreen(Day.today())) }
              }
            },
            icon = {
              Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = stringResource(R.string.cd_dailies_tab)
              )
            },
            label = { Text(stringResource(R.string.label_dailies)) })
          NavigationBarItem(
            selected = navStackEntry.destinationEquals(RoutineListScreen::class),
            onClick = {
              navStackEntry.destinationEquals(RoutineListScreen::class).onFalse {
                homeNavController.navigate(route = RoutineListScreen) {
                  popUpTo(DailiesScreen::class) { inclusive = false }
                }
              }
            },
            icon = {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = stringResource(R.string.cd_routines_tab)
              )
            },
            label = { Text(stringResource(R.string.label_routines)) })
          NavigationBarItem(
            selected = navStackEntry.destinationEquals(TrackedProgressListScreen::class),
            onClick = {
              navStackEntry.destinationEquals(TrackedProgressListScreen::class).onFalse {
                homeNavController.navigate(route = TrackedProgressListScreen) {
                  popUpTo(DailiesScreen::class) { inclusive = false }
                }
              }
            },
            icon = {
              Icon(
                painter = painterResource(R.drawable.baseline_bar_chart_24),
                contentDescription = stringResource(R.string.cd_progress_tracking_tab)
              )
            },
            label = { Text(stringResource(R.string.label_progress)) })
        }
      }
    }
  }
}