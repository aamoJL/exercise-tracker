package com.aamo.exercisetracker.features.monthlyProgress

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import kotlinx.serialization.Serializable

@Serializable
object MonthlyProgressPage

fun NavGraphBuilder.monthlyProgressPage() {
  navigation<MonthlyProgressPage>(startDestination = MonthlyProgressScreen) {
    monthlyProgressScreen()
    // TODO: progress form screen
  }
}