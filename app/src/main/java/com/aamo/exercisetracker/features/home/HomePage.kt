package com.aamo.exercisetracker.features.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable object HomePage

fun NavGraphBuilder.homePage(
  onSelectDaily: () -> Unit,
  onSelectWeekly: () -> Unit,
  onSelectMonthly: () -> Unit,
) {
  composable<HomePage> {
    HomeScreen(
      onTodayClick = onSelectDaily, onWeeklyClick = onSelectWeekly, onMonthlyClick = onSelectMonthly
    )
  }
}