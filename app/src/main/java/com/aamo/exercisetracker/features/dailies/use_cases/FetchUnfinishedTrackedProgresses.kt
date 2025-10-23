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
    map.filter { item ->
      if (item.key.intervalWeeks == 0) false
      else {
        item.value.maxByOrNull { value -> value.addedDate }?.addedDate?.time?.let { addedMillis -> currentTimeMillis - addedMillis >= item.key.intervalWeeks.weeks.inWholeMilliseconds }
          ?: true
      }
    }.map { it.key }
  }
}