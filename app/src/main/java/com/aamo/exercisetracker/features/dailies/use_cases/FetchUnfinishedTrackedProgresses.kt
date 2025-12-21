package com.aamo.exercisetracker.features.dailies.use_cases

import com.aamo.exercisetracker.database.dao.TrackedProgressDao
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.utility.extensions.date.weeks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun fetchUnfinishedTrackedProgressesFlow(
  dao: TrackedProgressDao, currentTimeMillis: Long
): Flow<List<TrackedProgress>> {
  return dao.getProgressesWithValuesFlow().map { map ->
    map.filter { (progress, values) ->
      if (progress.intervalWeeks == 0) false
      else {
        values.maxByOrNull { value -> value.addedDate }?.addedDate?.time?.let { addedMillis -> currentTimeMillis - addedMillis >= progress.intervalWeeks.weeks.inWholeMilliseconds }
          ?: true
      }
    }.map { (progress, _) -> progress }.sortedBy { it.name }
  }
}