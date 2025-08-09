package com.aamo.exercisetracker.utility.extensions.general

import kotlin.math.abs
import kotlin.math.log10

/**
 * Returns the number of digits
 */
fun Int.digits() = when (this) {
  0 -> 1
  else -> log10(abs(toDouble())).toInt() + 1
}