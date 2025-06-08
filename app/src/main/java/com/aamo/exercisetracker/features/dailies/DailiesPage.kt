package com.aamo.exercisetracker.features.dailies

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable data class DailiesPage(val initialDayNumber: Int = 1)

fun NavGraphBuilder.dailiesPage() {
  composable<DailiesPage> { navStack ->
    DailiesScreen(initialPage = (navStack.toRoute() as DailiesPage).initialDayNumber - 1)
  }
}

fun NavController.toDailiesPage(initialDayNumber: Int, builder: NavOptionsBuilder.() -> Unit = {}) {
  this.navigate(route = DailiesPage(initialDayNumber), builder = builder)
}