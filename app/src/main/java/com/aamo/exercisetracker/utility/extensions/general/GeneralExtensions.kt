package com.aamo.exercisetracker.utility.extensions.general

fun <T> applyIf(condition: Boolean, value: T): T? {
  return if (condition) value else null
}

fun Boolean.onFalse(block: (Boolean) -> Unit): Boolean {
  if (!this) {
    block(this)
  }
  return this
}