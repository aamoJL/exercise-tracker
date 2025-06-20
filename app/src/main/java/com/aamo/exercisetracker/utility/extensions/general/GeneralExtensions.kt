package com.aamo.exercisetracker.utility.extensions.general

fun <T> applyIf(condition: Boolean, value: T): T? {
  return if (condition) value else null
}