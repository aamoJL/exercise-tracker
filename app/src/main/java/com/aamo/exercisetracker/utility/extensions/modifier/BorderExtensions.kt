package com.aamo.exercisetracker.utility.extensions.modifier

import androidx.compose.foundation.BorderStroke

/**
 * Returns the BorderStroke if the condition is true, otherwise returns null
 */
fun BorderStroke.applyIf(condition: Boolean): BorderStroke? {
  return if (condition) this else null
}