package com.aamo.exercisetracker.features.progress_tracking.form.use_cases

import com.aamo.exercisetracker.database.dao.TrackedProgressDao
import com.aamo.exercisetracker.database.entities.TrackedProgress
import kotlin.math.max

suspend fun saveTrackedProgress(dao: TrackedProgressDao, model: TrackedProgress): Long {
  return max(dao.upsert(model), model.id)
}