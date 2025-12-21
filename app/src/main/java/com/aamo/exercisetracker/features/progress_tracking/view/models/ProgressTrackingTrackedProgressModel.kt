package com.aamo.exercisetracker.features.progress_tracking.view.models

import kotlin.time.Duration

data class ProgressTrackingTrackedProgressModel(
  val id: Long,
  val name: String,
  val progressType: ProgressType,
  val values: List<Int>,
  val recordUnit: String,
  val countdownTime: Duration?
) {
  enum class ProgressType {
    REPETITION,
    COUNTDOWN,
    STOPWATCH
  }
}