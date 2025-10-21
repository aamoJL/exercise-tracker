package com.aamo.exercisetracker.tests.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.database.entities.TrackedProgressWithValues
import com.aamo.exercisetracker.features.progress_tracking.ProgressTrackingScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.use_cases.fetchTrackedProgressFlow
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
class FetchTrackedProgress {
  @Test
  fun `returns correct model when timer`() = runBlocking {
    val item = TrackedProgressWithValues(
      trackedProgress = TrackedProgress(
        id = 2L,
        name = "Progress",
        intervalWeeks = 2,
        unit = "Unit",
        hasStopWatch = false,
        timerTime = 7.minutes.inWholeMilliseconds
      ), values = listOf(
        TrackedProgressValue(id = 1L, progressId = 2L, value = 10, addedDate = Date(10)),
        TrackedProgressValue(id = 2L, progressId = 2L, value = 5, addedDate = Date(5)),
        TrackedProgressValue(id = 3L, progressId = 2L, value = 12, addedDate = Date(12))
      )
    )
    val result = fetchTrackedProgressFlow(fetchData = { flow { emit(item) } }).first()

    assertEquals(
      ProgressTrackingScreenViewModel.Model(
        progressName = item.trackedProgress.name,
        recordValueUnit = item.trackedProgress.unit,
        records = item.values.sortedBy { it.addedDate }.map { it.value },
        recordType = ProgressTrackingScreenViewModel.Model.RecordType.TIMER,
        countDownTime = item.trackedProgress.timerTime?.milliseconds
      ), result
    )
  }

  @Test
  fun `returns correct model when repetitions`() = runBlocking {
    val item = TrackedProgressWithValues(
      trackedProgress = TrackedProgress(
        id = 2L,
        name = "Progress",
        intervalWeeks = 2,
        unit = "Unit",
        hasStopWatch = false,
        timerTime = null
      ), values = listOf(
        TrackedProgressValue(id = 1L, progressId = 2L, value = 10, addedDate = Date(10)),
        TrackedProgressValue(id = 2L, progressId = 2L, value = 5, addedDate = Date(5)),
        TrackedProgressValue(id = 3L, progressId = 2L, value = 12, addedDate = Date(12))
      )
    )
    val result = fetchTrackedProgressFlow(fetchData = { flow { emit(item) } }).first()

    assertEquals(
      ProgressTrackingScreenViewModel.Model(
        progressName = item.trackedProgress.name,
        recordValueUnit = item.trackedProgress.unit,
        records = item.values.sortedBy { it.addedDate }.map { it.value },
        recordType = ProgressTrackingScreenViewModel.Model.RecordType.REPETITION,
        countDownTime = null
      ), result
    )
  }

  @Test
  fun `returns correct model when stopwatch`() = runBlocking {
    val item = TrackedProgressWithValues(
      trackedProgress = TrackedProgress(
        id = 2L,
        name = "Progress",
        intervalWeeks = 2,
        unit = "Unit",
        hasStopWatch = true,
        timerTime = null
      ), values = listOf(
        TrackedProgressValue(id = 1L, progressId = 2L, value = 10, addedDate = Date(10)),
        TrackedProgressValue(id = 2L, progressId = 2L, value = 5, addedDate = Date(5)),
        TrackedProgressValue(id = 3L, progressId = 2L, value = 12, addedDate = Date(12))
      )
    )
    val result = fetchTrackedProgressFlow(fetchData = { flow { emit(item) } }).first()

    assertEquals(
      ProgressTrackingScreenViewModel.Model(
        progressName = item.trackedProgress.name,
        recordValueUnit = item.trackedProgress.unit,
        records = item.values.sortedBy { it.addedDate }.map { it.value },
        recordType = ProgressTrackingScreenViewModel.Model.RecordType.STOPWATCH,
        countDownTime = null
      ), result
    )
  }

  @Test
  fun `throws when fetch error`() {
    assertThrows(Exception::class.java) {
      runBlocking {
        fetchTrackedProgressFlow(fetchData = { flow { emit(null) } }).first()
      }
    }
  }
}