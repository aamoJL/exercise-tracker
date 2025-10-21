package com.aamo.exercisetracker.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgressWithValues
import com.aamo.exercisetracker.features.progress_tracking.ProgressTrackingScreenViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.milliseconds

fun fetchTrackedProgressFlow(
  fetchData: () -> Flow<TrackedProgressWithValues?>
): Flow<ProgressTrackingScreenViewModel.Model> {
  return fetchData().map { item ->
    if (item == null) throw Exception("Failed to fetch data")

    ProgressTrackingScreenViewModel.Model(
      progressName = item.trackedProgress.name,
      records = item.values.sortedBy { it.addedDate }.map { it.value },
      recordValueUnit = item.trackedProgress.unit,
      recordType = when {
        item.trackedProgress.hasStopWatch -> ProgressTrackingScreenViewModel.Model.RecordType.STOPWATCH
        item.trackedProgress.timerTime?.let { it > 0 } == true -> ProgressTrackingScreenViewModel.Model.RecordType.TIMER
        else -> ProgressTrackingScreenViewModel.Model.RecordType.REPETITION
      },
      countDownTime = item.trackedProgress.timerTime?.milliseconds)
  }
}