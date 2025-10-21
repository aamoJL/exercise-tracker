package com.aamo.exercisetracker.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgressWithValues
import com.aamo.exercisetracker.features.progress_tracking.TrackedProgressRecordListScreenViewModel

fun TrackedProgressRecordListScreenViewModel.Model.Companion.fromDao(item: TrackedProgressWithValues?): TrackedProgressRecordListScreenViewModel.Model {
  checkNotNull(item)

  return TrackedProgressRecordListScreenViewModel.Model(
    progressName = item.trackedProgress.name,
    valueUnit = item.trackedProgress.unit,
    values = item.values.sortedByDescending { it.addedDate }.map {
      TrackedProgressRecordListScreenViewModel.RecordModel(
        value = it.value, date = it.addedDate, key = it.id
      )
    },
    valueType = if (item.trackedProgress.hasStopWatch) TrackedProgressRecordListScreenViewModel.Model.ValueType.DURATION
    else TrackedProgressRecordListScreenViewModel.Model.ValueType.DEFAULT
  )
}