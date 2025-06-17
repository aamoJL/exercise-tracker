package com.aamo.exercisetracker.features.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aamo.exercisetracker.database.RoutineDatabase
import com.aamo.exercisetracker.features.dailies.DailiesScreen
import com.aamo.exercisetracker.features.routine.RoutineFormScreen
import com.aamo.exercisetracker.features.routine.RoutineListScreen
import com.aamo.exercisetracker.features.routine.RoutinePage
import com.aamo.exercisetracker.utility.extensions.date.today
import kotlinx.coroutines.flow.map
import java.util.Calendar
import kotlin.reflect.KClass

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(mainNavController: NavController) {
  val homeNavController = rememberNavController()
  val navStack = homeNavController.currentBackStackEntryAsState().value

  BackHandler(enabled = !navStack.destinationEquals(DailiesScreen::class)) {
    // Navigate back to the dailies tab, if pressed back on any other tab
    homeNavController.navigate(route = DailiesScreen(Calendar.getInstance().today())) {
      popUpTo(homeNavController.graph.id) { inclusive = true }
    }
  }

  Column {
    NavHost(
      navController = homeNavController,
      startDestination = DailiesScreen(Calendar.getInstance().today()),
      modifier = Modifier.weight(1f)
    ) {
      composable<DailiesScreen> { stack ->
        DailiesScreen(
          initialPage = stack.toRoute<DailiesScreen>().initialDayNumber - 1,
          modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        )
      }
      composable<RoutineListScreen> {
        val routines by RoutineDatabase.getDatabase(LocalContext.current.applicationContext)
          .routineDao().getRoutinesWithScheduleFlow()
          .map { list -> list.sortedBy { it.routine.name } }
          .collectAsStateWithLifecycle(initialValue = emptyList())

        RoutineListScreen(
          routines = routines,
          onSelectRoutine = { id -> mainNavController.navigate(RoutinePage(id = id)) },
          onAdd = { mainNavController.navigate(RoutineFormScreen(id = 0L)) })
      }
    }

    // prevents bottom bar to be visible when IME is visible
    if (!WindowInsets.isImeVisible) {
      BottomAppBar {
        NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
          NavigationBarItem(selected = navStack.destinationEquals(DailiesScreen::class), onClick = {
            if (!navStack.destinationEquals(DailiesScreen::class)) {
              homeNavController.navigate(route = DailiesScreen(Calendar.getInstance().today())) {
                popUpTo(homeNavController.graph.id) { inclusive = true }
              }
            }
          }, icon = {
            Icon(
              imageVector = Icons.Filled.DateRange, contentDescription = "Dailies tab"
            )
          }, label = { Text("Dailies") })
          NavigationBarItem(
            selected = navStack.destinationEquals(RoutineListScreen::class),
            onClick = {
              if (!navStack.destinationEquals(RoutineListScreen::class)) {
                homeNavController.navigate(route = RoutineListScreen) {
                  popUpTo(homeNavController.graph.id) { inclusive = true }
                }
              }
            },
            icon = {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.List, contentDescription = "Routines tab"
              )
            },
            label = { Text("Routines") })
        }
      }
    }
  }
}

private fun <T : Any> NavBackStackEntry?.destinationEquals(route: KClass<T>): Boolean {
  return this?.destination?.hasRoute(route) == true
}