package com.aamo.exercisetracker.utility.extensions.date

import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import com.aamo.exercisetracker.utility.extensions.general.letIf
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Returns duration in 00:00 format
 */
fun Duration.toClockString(
  hasSeconds: Boolean = true,
  hasMinutes: Boolean = true,
  hasHours: Boolean = false,
  separatorChar: Char = ':'
): String {
  return String.EMPTY.letIf(hasHours) { text ->
    text.plus((this.inWholeHours % 24).toString().padStart(2, '0'))
  }.letIf(hasMinutes) { text ->
    text.letIf(text.isNotEmpty()) { text ->
      text.plus(separatorChar)
    }.plus((this.inWholeMinutes % 60).toString().padStart(2, '0'))
  }.letIf(hasSeconds) { text ->
    text.letIf(text.isNotEmpty()) { text ->
      text.plus(separatorChar)
    }.plus((this.inWholeSeconds % 60).toString().padStart(2, '0'))
  }
}

data class DurationSegments(val seconds: Int, val minutes: Int, val hours: Int) {
  fun toDuration(): Duration {
    return seconds.seconds + minutes.minutes + hours.hours
  }
}