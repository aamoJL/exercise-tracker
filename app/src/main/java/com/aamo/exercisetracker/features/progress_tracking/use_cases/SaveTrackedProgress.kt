package com.aamo.exercisetracker.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.TrackedProgressFormScreenViewModel

suspend fun saveTrackedProgress(
  progressId: Long,
  model: TrackedProgressFormScreenViewModel.Model,
  saveData: suspend (TrackedProgress) -> Boolean
): Boolean {
  return saveData(
    TrackedProgress(
      id = progressId,
      name = model.trackedProgressName,
      intervalWeeks = model.weeklyInterval,
      unit = model.progressValueUnit,
      hasStopWatch = model.hasStopWatch,
      timerTime = model.timerDuration?.inWholeMilliseconds
    )
  )
}