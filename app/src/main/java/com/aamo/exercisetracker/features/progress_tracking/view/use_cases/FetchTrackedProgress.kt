package com.aamo.exercisetracker.features.progress_tracking.view.use_cases

import com.aamo.exercisetracker.database.dao.TrackedProgressDao
import com.aamo.exercisetracker.features.progress_tracking.view.models.ProgressTrackingTrackedProgressModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.milliseconds

fun fetchTrackedProgressFlow(
  dao: TrackedProgressDao, progressId: Long
): Flow<ProgressTrackingTrackedProgressModel> {
  return (dao.getProgressWithValuesFlow(progressId)).map {
    it ?: throw IllegalStateException("Failed to fetch data")
  }.map { (progress, values) ->
    ProgressTrackingTrackedProgressModel(
      id = progress.id,
      name = progress.name,
      values = values.sortedBy { it.addedDate }.map { it.value },
      recordUnit = progress.unit,
      progressType = if (progress.hasStopWatch) ProgressTrackingTrackedProgressModel.ProgressType.STOPWATCH
      else if (progress.timerTime?.let { it > 0 } == true) ProgressTrackingTrackedProgressModel.ProgressType.COUNTDOWN
      else ProgressTrackingTrackedProgressModel.ProgressType.REPETITION,
      countdownTime = progress.timerTime?.milliseconds)
  }
}