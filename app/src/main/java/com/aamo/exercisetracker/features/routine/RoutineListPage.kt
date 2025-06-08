package com.aamo.exercisetracker.features.routine

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import kotlinx.serialization.Serializable

@Serializable object RoutineListPage

fun NavGraphBuilder.routineListPage(
  navController: NavController, onBack: () -> Unit, onSelectRoutine: (id: Int) -> Unit
) {
  navigation<RoutineListPage>(startDestination = RoutineListScreen) {
    routinesListScreen(
      onBack = onBack,
      onAddRoutine = { navController.toRoutineFormScreen(0) },
      onSelectRoutine = onSelectRoutine
    )
    routineFormScreen(onBack = { navController.popBackStack() }, onSave = {
      /* TODO: routine save command */
    })
  }
}

fun NavController.toRoutinesPage() {
  this.navigate(route = RoutineListPage)
}