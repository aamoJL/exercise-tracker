package com.aamo.exercisetracker.tests.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.database.entities.TrackedProgressWithValues
import com.aamo.exercisetracker.features.progress_tracking.ProgressTrackingScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.TrackedProgressFormScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.use_cases.fromDao
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
class ProgressTrackingScreenViewModelTests {
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
    val result = ProgressTrackingScreenViewModel.Model.fromDao {
      flow { emit(item) }
    }.first()

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
    val result = ProgressTrackingScreenViewModel.Model.fromDao {
      flow { emit(item) }
    }.first()

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
    val result = ProgressTrackingScreenViewModel.Model.fromDao {
      flow { emit(item) }
    }.first()

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
}

@Suppress("HardCodedStringLiteral")
class TrackedProgressFormScreenViewModelTests {
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

    val result = TrackedProgressFormScreenViewModel.Model.fromDao(defaultUnit = defaultUnit) {
      TrackedProgress()
    }

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

    val actual = TrackedProgressFormScreenViewModel.Model.fromDao(defaultUnit = String.EMPTY) {
      existing
    }

    assertEquals(expected, actual)
  }
}