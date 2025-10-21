package com.aamo.exercisetracker.tests.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.TrackedProgressFormScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.use_cases.fetchTrackedProgressFormData
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
class FetchTrackedProgressFormData {
  @Test
  fun `returns correct model when new`() = runBlocking {
    val defaultUnit = "Default Unit"
    val model = TrackedProgressFormScreenViewModel.Model(
      trackedProgressName = String.EMPTY,
      weeklyInterval = 0,
      progressValueUnit = defaultUnit,
      hasStopWatch = false,
      timerDuration = null,
      isNew = true
    )

    val result = fetchTrackedProgressFormData(
      progressId = 0L, defaultUnit = defaultUnit, fetchData = { null })

    assertEquals(model, result)
  }

  @Test
  fun `returns correct model when existing`() = runBlocking {
    val existing = TrackedProgress(
      id = 1L,
      name = "Name",
      intervalWeeks = 3,
      unit = "Unit",
      hasStopWatch = false,
      timerTime = 4.minutes.inWholeMilliseconds
    )
    val expected = TrackedProgressFormScreenViewModel.Model(
      trackedProgressName = existing.name,
      weeklyInterval = existing.intervalWeeks,
      progressValueUnit = existing.unit,
      hasStopWatch = existing.hasStopWatch,
      timerDuration = existing.timerTime?.milliseconds,
      isNew = false
    )

    val actual = fetchTrackedProgressFormData(
      progressId = existing.id, defaultUnit = String.EMPTY, fetchData = { existing })

    assertEquals(expected, actual)
  }

  @Test
  fun `throws when fetch error`() {
    assertThrows(Exception::class.java) {
      runBlocking {
        fetchTrackedProgressFormData(
          progressId = 1L, defaultUnit = String.EMPTY, fetchData = { null })
      }
    }
  }
}