package com.aamo.exercisetracker.features.progress_tracking.records.use_cases

import com.aamo.exercisetracker.database.dao.TrackedProgressDao
import com.aamo.exercisetracker.features.progress_tracking.records.models.TrackedProgressRecordListModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun fetchTrackedProgressRecordsFlow(
  dao: TrackedProgressDao, progressId: Long
): Flow<TrackedProgressRecordListModel> {
  return dao.getProgressWithValuesFlow(progressId)
    .map { it ?: throw IllegalStateException("Failed to fetch data") }.map { (progress, values) ->
      TrackedProgressRecordListModel(
        progressName = progress.name,
        valueUnit = progress.unit,
        records = values.sortedByDescending { it.addedDate },
        valueType = if (progress.hasStopWatch) TrackedProgressRecordListModel.ValueType.DURATION
        else TrackedProgressRecordListModel.ValueType.COUNT
      )
    }
}