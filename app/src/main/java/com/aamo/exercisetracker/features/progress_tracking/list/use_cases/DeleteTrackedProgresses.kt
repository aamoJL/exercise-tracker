package com.aamo.exercisetracker.features.progress_tracking.list.use_cases

import com.aamo.exercisetracker.database.dao.TrackedProgressDao
import com.aamo.exercisetracker.database.entities.TrackedProgress

suspend fun deleteTrackedProgresses(dao: TrackedProgressDao, vararg models: TrackedProgress) {
  dao.delete(*models)
}