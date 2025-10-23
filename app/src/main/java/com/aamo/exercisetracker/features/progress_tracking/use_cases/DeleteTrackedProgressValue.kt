package com.aamo.exercisetracker.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.features.progress_tracking.TrackedProgressRecordListScreenViewModel

suspend fun deleteTrackedProgressValue(
  vararg values: TrackedProgressValue, deleteData: suspend (List<TrackedProgressValue>) -> Boolean
) {
  deleteData(values.toList())
}

fun TrackedProgressRecordListScreenViewModel.RecordModel.toDao(progressId: Long): TrackedProgressValue {
  return TrackedProgressValue(
    id = this.key, progressId = progressId, value = this.value, addedDate = this.date
  )
}