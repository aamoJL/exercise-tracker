package com.aamo.exercisetracker.utility.extensions.general

import org.jetbrains.annotations.NotNull

fun <T> applyIf(condition: Boolean, value: T): T? {
  return if (condition) value else null
}

inline fun <T> T.letIf(condition: Boolean, block: (T) -> T): T {
  return if (condition) {
    block(this)
  }
  else this
}

inline fun Boolean.onFalse(block: (Boolean) -> Unit): Boolean {
  if (!this) {
    block(this)
  }
  return this
}

inline fun Boolean.onTrue(block: (Boolean) -> Unit): Boolean {
  if (this) {
    block(this)
  }
  return this
}

inline fun <T> T?.onNotNull(block: (@NotNull T) -> Unit): T? {
  if (this != null) {
    block(this)
  }
  return this
}

inline fun <T> T.onNull(block: () -> Unit): T {
  if (this == null) {
    block()
  }
  return this
}

fun <T> ifElse(condition: Boolean, onTrue: T, onFalse: T): T {
  return if (condition) onTrue else onFalse
}