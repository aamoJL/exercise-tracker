package com.aamo.exercisetracker.tests.features.progress_tracking.view.progress_tracking_screen_viewmodel.stopwatch

import com.aamo.exercisetracker.features.progress_tracking.view.ProgressTrackingScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.view.models.ProgressTrackingTrackedProgressModel
import com.aamo.exercisetracker.services.IStopwatchTimerService
import com.aamo.exercisetracker.utility.viewmodels.IClock
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@Suppress("HardCodedStringLiteral")
@OptIn(ExperimentalCoroutinesApi::class)
class StopStopwatch {
  @Test
  fun `stopwatch stopped`() = runTest(UnconfinedTestDispatcher()) {
    val data = ProgressTrackingTrackedProgressModel(
      id = 1L,
      name = "Exercise 1",
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.STOPWATCH,
      values = emptyList(),
      recordUnit = "Unit",
      countdownTime = null
    )
    val viewmodel =
      ProgressTrackingScreenViewModel(fetchData = { flow { emit(data) } }, addValue = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    TestCase.assertFalse(viewmodel.stopwatchTimerState.isActive.value)

    viewmodel.startStopwatch()
    TestCase.assertTrue(viewmodel.stopwatchTimerState.isActive.value)

    viewmodel.stopStopwatch()
    TestCase.assertFalse(viewmodel.stopwatchTimerState.isActive.value)
  }

  @Test
  fun `background service cancelled`() = runTest(UnconfinedTestDispatcher()) {
    var bgCancelled = false
    val data = ProgressTrackingTrackedProgressModel(
      id = 1L,
      name = "Exercise 1",
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.STOPWATCH,
      values = emptyList(),
      recordUnit = "Unit",
      countdownTime = null
    )
    val viewmodel =
      ProgressTrackingScreenViewModel(fetchData = { flow { emit(data) } }, addValue = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    TestCase.assertFalse(bgCancelled)

    viewmodel.startStopwatch(backgroundService = object : IStopwatchTimerService {
      override fun cancel() {
        bgCancelled = true
      }
    })

    viewmodel.stopStopwatch()
    TestCase.assertTrue(bgCancelled)
  }

  @OptIn(ExperimentalTime::class)
  @Test
  fun `final duration is correct`() = runTest(UnconfinedTestDispatcher()) {
    val start = 0L
    val clock = object : IClock {
      var time: Long = start

      override fun now(): Long {
        return time
      }
    }

    val data = ProgressTrackingTrackedProgressModel(
      id = 1L,
      name = "Exercise 1",
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.STOPWATCH,
      values = emptyList(),
      recordUnit = "Unit",
      countdownTime = null
    )
    val viewmodel = ProgressTrackingScreenViewModel(
      fetchData = { flow { emit(data) } },
      addValue = { fail() },
      clock = clock
    )

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    assertEquals(0.seconds, viewmodel.stopwatchTimerState.finalDuration.value)

    viewmodel.startStopwatch()
    assertEquals(0.seconds, viewmodel.stopwatchTimerState.finalDuration.value)

    val end = 4.minutes.inWholeMilliseconds
    clock.time = end

    viewmodel.stopStopwatch()
    assertEquals((end - start).milliseconds, viewmodel.stopwatchTimerState.finalDuration.value)
  }
}