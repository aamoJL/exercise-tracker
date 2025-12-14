package com.aamo.exercisetracker.features.progress_tracking.form.use_cases

import com.aamo.exercisetracker.database.dao.TrackedProgressDao
import com.aamo.exercisetracker.database.entities.TrackedProgress

suspend fun fetchTrackedProgress(
  dao: TrackedProgressDao, progressId: Long, defaultUnit: String
): TrackedProgress? {
  return if (progressId == 0L) TrackedProgress(unit = defaultUnit)
  else dao.getTrackedProgress(progressId)
}