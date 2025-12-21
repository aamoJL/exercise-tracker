package com.aamo.exercisetracker.features.progress_tracking.records.use_cases

import com.aamo.exercisetracker.database.dao.TrackedProgressDao
import com.aamo.exercisetracker.database.entities.TrackedProgressValue

suspend fun deleteTrackedProgressRecord(dao: TrackedProgressDao, record: TrackedProgressValue) {
  dao.delete(record)
}