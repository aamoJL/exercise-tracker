package com.aamo.exercisetracker.utility.extensions.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlin.reflect.KClass

fun <T : Any> NavBackStackEntry?.destinationEquals(route: KClass<T>): Boolean {
  return this?.destination?.hasRoute(route) == true
}