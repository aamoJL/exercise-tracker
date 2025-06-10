package com.aamo.exercisetracker.features.home

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable object HomePage

fun NavGraphBuilder.homePage(navController: NavController) {
  composable<HomePage> {
    HomeScreen(mainNavController = navController)
  }
}