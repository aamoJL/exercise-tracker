package com.aamo.exercisetracker.features.progress_tracking.list.use_cases

import com.aamo.exercisetracker.database.dao.TrackedProgressDao
import com.aamo.exercisetracker.database.entities.TrackedProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun fetchTrackedProgressesFlow(dao: TrackedProgressDao): Flow<List<TrackedProgress>> {
  return dao.getProgressesFlow().map { list -> list.sortedBy { it.name } }
}