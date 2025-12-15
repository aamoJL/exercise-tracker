package com.aamo.exercisetracker.tests.features.progress_tracking.view.progress_tracking_screen_viewmodel

import com.aamo.exercisetracker.features.progress_tracking.view.ProgressTrackingScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.view.models.ProgressTrackingTrackedProgressModel
import com.aamo.exercisetracker.services.ICountDownTimerService
import com.aamo.exercisetracker.services.IStopwatchTimerService
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("HardCodedStringLiteral")
class CancelTimer : UnconfinedTest() {
  @Test
  fun `countdown cancelled`() = runTest(UnconfinedTestDispatcher()) {
    var cancelled = false
    val model = ProgressTrackingTrackedProgressModel(
      id = 1L,
      name = "Progress 1",
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.COUNTDOWN,
      records = emptyList(),
      recordUnit = "Unit",
      countDownTime = 4.minutes
    )
    val viewmodel =
      ProgressTrackingScreenViewModel(fetchData = { flow { emit(model) } }, addValue = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    viewmodel.cancelTimer(countDownTimerService = object : ICountDownTimerService {
      override fun cancel() {
        cancelled = true
      }
    })
    assertTrue(cancelled)
  }

  @Test
  fun `stopwatch cancelled`() = runTest(UnconfinedTestDispatcher()) {
    var cancelled = false
    val model = ProgressTrackingTrackedProgressModel(
      id = 1L,
      name = "Progress 1",
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.STOPWATCH,
      records = emptyList(),
      recordUnit = "Unit",
      countDownTime = null
    )
    val viewmodel =
      ProgressTrackingScreenViewModel(fetchData = { flow { emit(model) } }, addValue = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    viewmodel.cancelTimer(stopwatchTimerService = object : IStopwatchTimerService {
      override fun cancel() {
        cancelled = true
      }
    })
    assertTrue(cancelled)
  }
}