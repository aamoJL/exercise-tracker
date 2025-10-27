package com.aamo.exercisetracker.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.TrackedProgressFormScreenViewModel

suspend fun saveTrackedProgress(
  data: TrackedProgress, saveData: suspend (TrackedProgress) -> Boolean
): Boolean {
  return saveData(data)
}

fun TrackedProgressFormScreenViewModel.Model.toDao(progressId: Long): TrackedProgress {
  return TrackedProgress(
    id = progressId,
    name = this.trackedProgressName,
    intervalWeeks = this.weeklyInterval,
    unit = this.progressValueUnit,
    hasStopWatch = this.hasStopWatch,
    timerTime = this.timerDuration?.inWholeMilliseconds
  )
}