package com.aamo.exercisetracker.utility.extensions.general

import org.jetbrains.annotations.NotNull

inline fun <T> applyIf(condition: Boolean, value: () -> T): T? {
  return if (condition) value() else null
}

inline fun <T> T.letIf(condition: Boolean, block: (T) -> T): T {
  return if (condition) {
    block(this)
  }
  else this
}

inline fun Boolean.onFalse(block: () -> Unit): Boolean {
  if (!this) {
    block()
  }
  return this
}

inline fun Boolean.onTrue(block: () -> Unit): Boolean {
  if (this) {
    block()
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

inline fun <T> ifElse(condition: Boolean, ifTrue: () -> T, ifFalse: () -> T): T {
  return if (condition) ifTrue() else ifFalse()
}