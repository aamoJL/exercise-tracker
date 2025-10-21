package com.aamo.exercisetracker.tests.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.TrackedProgressFormScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.use_cases.saveTrackedProgress
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
class SaveTrackedProgress {
  @Test
  fun `returns correct model when saving new`() = runBlocking {
    var result: TrackedProgress? = null
    val model = TrackedProgressFormScreenViewModel.Model(
      trackedProgressName = "Name",
      weeklyInterval = 4,
      progressValueUnit = "Unit",
      hasStopWatch = false,
      timerDuration = 8.minutes,
      isNew = true
    )
    val expected = TrackedProgress(
      id = 0L,
      name = model.trackedProgressName,
      intervalWeeks = model.weeklyInterval,
      unit = model.progressValueUnit,
      hasStopWatch = model.hasStopWatch,
      timerTime = model.timerDuration?.inWholeMilliseconds
    )

    assert(saveTrackedProgress(progressId = expected.id, model = model, saveData = {
      result = it; true
    }))

    assertEquals(expected, result)
  }

  @Test
  fun `returns correct model when saving existing`() = runBlocking {
    var result: TrackedProgress? = null
    val model = TrackedProgressFormScreenViewModel.Model(
      trackedProgressName = "Name",
      weeklyInterval = 4,
      progressValueUnit = "Unit",
      hasStopWatch = false,
      timerDuration = 8.minutes,
      isNew = false
    )
    val expected = TrackedProgress(
      id = 1L,
      name = model.trackedProgressName,
      intervalWeeks = model.weeklyInterval,
      unit = model.progressValueUnit,
      hasStopWatch = model.hasStopWatch,
      timerTime = model.timerDuration?.inWholeMilliseconds
    )

    assert(saveTrackedProgress(progressId = expected.id, model = model, saveData = {
      result = it; true
    }))

    assertEquals(expected, result)
  }
}