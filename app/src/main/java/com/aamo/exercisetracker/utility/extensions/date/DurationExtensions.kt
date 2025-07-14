package com.aamo.exercisetracker.utility.extensions.date

import com.aamo.exercisetracker.utility.extensions.general.letIf
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import kotlin.time.Duration

/**
 * Returns duration in 00:00 format
 */
fun Duration.toClockString(
  hasSeconds: Boolean = true, hasMinutes: Boolean = true, separatorChar: Char = ':'
): String {
  return String.EMPTY.letIf(hasMinutes) { text ->
    text.plus(this.inWholeMinutes.toString().padStart(2, '0'))
  }.letIf(hasSeconds) { text ->
    text.letIf(text.isNotEmpty()) { text ->
      text.plus(separatorChar)
    }.plus((this.inWholeSeconds % 60).toString().padStart(2, '0'))
  }
}