package com.aamo.exercisetracker.features.exercise.form.models

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class ExerciseFormFields(
  val name: String,
  val restDuration: Duration,
  val unit: String,
  val setValues: List<Int>,
  val hasTimer: Boolean,
) {
  val hasRest = restDuration > 0.seconds
}