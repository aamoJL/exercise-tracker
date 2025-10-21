package com.aamo.exercisetracker.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.TrackedProgressFormScreenViewModel
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import kotlin.time.Duration.Companion.milliseconds

suspend fun fetchTrackedProgressFormData(
  progressId: Long, defaultUnit: String, fetchData: suspend (id: Long) -> TrackedProgress?
): TrackedProgressFormScreenViewModel.Model {
  return if (progressId == 0L) newModel(defaultUnit = defaultUnit)
  else existingModel(fetchData = {
    fetchData(progressId)
  })
}

private fun newModel(
  defaultUnit: String,
): TrackedProgressFormScreenViewModel.Model {
  return TrackedProgressFormScreenViewModel.Model(
    trackedProgressName = String.EMPTY,
    weeklyInterval = 0,
    progressValueUnit = defaultUnit,
    hasStopWatch = false,
    timerDuration = null,
    isNew = true
  )
}

private suspend fun existingModel(
  fetchData: suspend () -> TrackedProgress?
): TrackedProgressFormScreenViewModel.Model {
  return (fetchData() ?: throw Exception("Failed to fetch data")).let { progress ->
    TrackedProgressFormScreenViewModel.Model(
      trackedProgressName = progress.name,
      weeklyInterval = progress.intervalWeeks,
      progressValueUnit = progress.unit,
      hasStopWatch = progress.hasStopWatch,
      timerDuration = progress.timerTime?.milliseconds,
      isNew = false
    )
  }
}