package com.aamo.exercisetracker.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgressValue

suspend fun saveTrackedProgressValue(
  value: TrackedProgressValue,
  saveData: suspend (TrackedProgressValue) -> Boolean,
): Boolean {
  return saveData(value)
}