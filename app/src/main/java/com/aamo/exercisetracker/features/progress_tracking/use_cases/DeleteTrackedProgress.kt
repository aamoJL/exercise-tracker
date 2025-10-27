package com.aamo.exercisetracker.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress

suspend fun deleteTrackedProgress(
  vararg values: TrackedProgress,
  deleteData: suspend (List<TrackedProgress>) -> Boolean,
): Boolean {
  return deleteData(values.toList())
}