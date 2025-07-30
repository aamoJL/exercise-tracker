package com.aamo.exercisetracker.features.monthlyProgress

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object MonthlyProgressScreen

fun NavGraphBuilder.monthlyProgressScreen() {
  composable<MonthlyProgressScreen> {
    // TODO: progress screen
  }
}