package com.aamo.exercisetracker.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressWithValues
import com.aamo.exercisetracker.features.progress_tracking.ProgressTrackingScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.TrackedProgressFormScreenViewModel
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.milliseconds

fun ProgressTrackingScreenViewModel.Model.Companion.fromDao(
  fetchData: () -> Flow<TrackedProgressWithValues>
): Flow<ProgressTrackingScreenViewModel.Model> {
  return fetchData().map { item ->
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

suspend fun TrackedProgressFormScreenViewModel.Model.Companion.fromDao(
  defaultUnit: String, fetchData: suspend () -> TrackedProgress
): TrackedProgressFormScreenViewModel.Model {
  return fetchData().let { result ->
    if (result.id == 0L) {
      TrackedProgressFormScreenViewModel.Model(
        trackedProgressName = String.EMPTY,
        weeklyInterval = 0,
        progressValueUnit = defaultUnit,
        hasStopWatch = false,
        timerDuration = null,
        isNew = true
      )
    }
    else {
      TrackedProgressFormScreenViewModel.Model(
        trackedProgressName = result.name,
        weeklyInterval = result.intervalWeeks,
        progressValueUnit = result.unit,
        hasStopWatch = result.hasStopWatch,
        timerDuration = result.timerTime?.milliseconds,
        isNew = false
      )
    }
  }
}