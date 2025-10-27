package com.aamo.exercisetracker.features.dailies.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.utility.extensions.date.weeks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun fetchUnfinishedTrackedProgressesFlow(
  currentTimeMillis: Long, getDataFlow: () -> Flow<Map<TrackedProgress, List<TrackedProgressValue>>>
): Flow<List<TrackedProgress>> {
  return getDataFlow().map { map ->
    map.filter { (progress, values) ->
      if (progress.intervalWeeks == 0) false
      else {
        values.maxByOrNull { value -> value.addedDate }?.addedDate?.time?.let { addedMillis -> currentTimeMillis - addedMillis >= progress.intervalWeeks.weeks.inWholeMilliseconds }
          ?: true
      }
    }.map { (progress, _) -> progress }.sortedBy { it.name }
  }
}