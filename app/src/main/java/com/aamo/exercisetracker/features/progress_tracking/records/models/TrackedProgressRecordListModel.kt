package com.aamo.exercisetracker.features.progress_tracking.records.models

import com.aamo.exercisetracker.database.entities.TrackedProgressValue

data class TrackedProgressRecordListModel(
  val progressName: String,
  val valueUnit: String,
  val records: List<TrackedProgressValue>,
  val valueType: ValueType
) {
  enum class ValueType {
    COUNT,
    DURATION
  }
}