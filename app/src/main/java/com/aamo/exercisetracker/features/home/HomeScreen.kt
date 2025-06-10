package com.aamo.exercisetracker.features.home

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aamo.exercisetracker.features.dailies.DailiesScreen
import com.aamo.exercisetracker.features.routine.RoutineFormScreen
import com.aamo.exercisetracker.features.routine.RoutineListScreen
import com.aamo.exercisetracker.features.routine.RoutinePage
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(mainNavController: NavController) {
  val homeNavController = rememberNavController()
  var selectedNavBarIndex by rememberSaveable { mutableIntStateOf(0) }

  Column {
    NavHost(
      navController = homeNavController,
      startDestination = DailiesScreen(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)),
      modifier = Modifier.weight(1f)
    ) {
      composable<DailiesScreen> { stack ->
        DailiesScreen(
          initialPage = stack.toRoute<DailiesScreen>().initialDayNumber - 1,
          modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        )
      }
      composable<RoutineListScreen> {
        RoutineListScreen(
          onSelectRoutine = { mainNavController.navigate(RoutinePage(id = id)) },
          onAdd = { mainNavController.navigate(RoutineFormScreen(id = 0)) })
      }
    }

    // prevents bottom bar to be visible when IME is visible
    if (!WindowInsets.isImeVisible) {
      BottomAppBar {
        NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
          NavigationBarItem(selected = selectedNavBarIndex == 0, onClick = {
            if (selectedNavBarIndex != 0) {
              selectedNavBarIndex = 0
              homeNavController.popBackStack()
              homeNavController.navigate(
                route = DailiesScreen(
                  Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                )
              )
            }
          }, icon = {
            Icon(
              imageVector = Icons.Filled.DateRange, contentDescription = "Dailies tab"
            )
          }, label = { Text("Dailies") })
          NavigationBarItem(selected = selectedNavBarIndex == 1, onClick = {
            if (selectedNavBarIndex != 1) {
              selectedNavBarIndex = 1
              homeNavController.popBackStack()
              homeNavController.navigate(route = RoutineListScreen) {

              }
            }
          }, icon = {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.List, contentDescription = "Routines tab"
            )
          }, label = { Text("Routines") })
        }
      }
    }
  }
}