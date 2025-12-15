package com.aamo.exercisetracker.features.progress_tracking.view.use_cases

import com.aamo.exercisetracker.database.dao.TrackedProgressDao
import com.aamo.exercisetracker.database.entities.TrackedProgressValue

suspend fun saveTrackedProgressValue(dao: TrackedProgressDao, value: TrackedProgressValue) {
  dao.upsert(value)
}