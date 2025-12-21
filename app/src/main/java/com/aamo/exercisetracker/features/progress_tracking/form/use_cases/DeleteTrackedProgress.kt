package com.aamo.exercisetracker.features.progress_tracking.form.use_cases

import com.aamo.exercisetracker.database.dao.TrackedProgressDao
import com.aamo.exercisetracker.database.entities.TrackedProgress

suspend fun deleteTrackedProgress(dao: TrackedProgressDao, model: TrackedProgress) {
  dao.delete(model)
}