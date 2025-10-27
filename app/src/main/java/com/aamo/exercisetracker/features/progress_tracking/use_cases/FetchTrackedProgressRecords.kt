package com.aamo.exercisetracker.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgressWithValues
import com.aamo.exercisetracker.features.progress_tracking.TrackedProgressRecordListScreenViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

suspend fun TrackedProgressRecordListScreenViewModel.Model.Companion.fromDao(
  fetchData: suspend () -> Flow<TrackedProgressWithValues>
): Flow<TrackedProgressRecordListScreenViewModel.Model> {
  return fetchData().map { result ->
    TrackedProgressRecordListScreenViewModel.Model(
      progressName = result.trackedProgress.name,
      valueUnit = result.trackedProgress.unit,
      values = result.values.sortedByDescending { it.addedDate }.map {
        TrackedProgressRecordListScreenViewModel.RecordModel(
          value = it.value, date = it.addedDate, key = it.id
        )
      },
      valueType = if (result.trackedProgress.hasStopWatch) TrackedProgressRecordListScreenViewModel.Model.ValueType.DURATION
      else TrackedProgressRecordListScreenViewModel.Model.ValueType.DEFAULT
    )
  }
}