package com.aamo.exercisetracker.tests.features.progress_tracking.view.progress_tracking_screen_viewmodel.stopwatch

import com.aamo.exercisetracker.features.progress_tracking.view.ProgressTrackingScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.view.models.ProgressTrackingTrackedProgressModel
import com.aamo.exercisetracker.services.IStopwatchTimerService
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import junit.framework.TestCase
import junit.framework.TestCase.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@Suppress("HardCodedStringLiteral")
@OptIn(ExperimentalCoroutinesApi::class)
class StartStopwatch : UnconfinedTest() {
  @Test
  fun `stopwatch started`() = runTest(UnconfinedTestDispatcher()) {
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
  }

  @Test
  fun `background service started`() = runTest(UnconfinedTestDispatcher()) {
    var bgStarted = false
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

    TestCase.assertFalse(bgStarted)

    viewmodel.startStopwatch(backgroundService = object : IStopwatchTimerService {
      override fun start(startTime: Long) {
        bgStarted = true
      }
    })
    TestCase.assertTrue(bgStarted)
  }

  @Test
  fun `start time is correct`() = runTest(UnconfinedTestDispatcher()) {
    var actual: Long? = null
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

    TestCase.assertNull(actual)
    viewmodel.startStopwatch(backgroundService = object : IStopwatchTimerService {
      override fun start(startTime: Long) {
        actual = startTime
      }
    })
    TestCase.assertEquals(System.currentTimeMillis(), actual)
  }
}