package com.aamo.exercisetracker.features.progress_tracking.form.models

import kotlin.time.Duration

data class TrackedProgressFormFields(
  val name: String,
  val weeklyInterval: Int,
  val type: ProgressType,
  val progressValueUnit: String,
  val timerDuration: Duration?,
) {
  enum class ProgressType {
    REPETITION,
    TIMER,
    STOPWATCH
  }
}