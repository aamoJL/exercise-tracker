package com.aamo.exercisetracker.tests.features.progress_tracking.view.progress_tracking_screen_viewmodel.countdown

import com.aamo.exercisetracker.features.progress_tracking.view.ProgressTrackingScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.view.models.ProgressTrackingTrackedProgressModel
import com.aamo.exercisetracker.services.ICountdownTimerService
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import com.aamo.exercisetracker.utility.viewmodels.ITimer
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
@OptIn(ExperimentalCoroutinesApi::class)
class CancelCountdown : UnconfinedTest() {
  @Test
  fun `countdown stopped`() = runTest(UnconfinedTestDispatcher()) {
    var timerCleaned = false
    val data = ProgressTrackingTrackedProgressModel(
      id = 1L,
      name = "Exercise 1",
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.COUNTDOWN,
      values = emptyList(),
      recordUnit = "Unit",
      countdownTime = 3.minutes
    )
    val viewmodel = ProgressTrackingScreenViewModel(
      fetchData = { flow { emit(data) } },
      addValue = { TestCase.fail() },
      timer = object : ITimer {
        override fun cleanup() {
          timerCleaned = true
        }
      })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    viewmodel.startCountdown()
    TestCase.assertFalse(timerCleaned)
    TestCase.assertTrue(viewmodel.countdownTimerState.isActive.value)


    viewmodel.cancelCountdown()
    TestCase.assertTrue(timerCleaned)
    TestCase.assertFalse(viewmodel.countdownTimerState.isActive.value)
  }

  @Test
  fun `background service cancelled`() = runTest(UnconfinedTestDispatcher()) {
    var bgCancelled = false
    val data = ProgressTrackingTrackedProgressModel(
      id = 1L,
      name = "Exercise 1",
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.COUNTDOWN,
      values = emptyList(),
      recordUnit = "Unit",
      countdownTime = 3.minutes
    )
    val viewmodel = ProgressTrackingScreenViewModel(
      fetchData = { flow { emit(data) } },
      addValue = { TestCase.fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }


    viewmodel.startCountdown(backgroundService = object : ICountdownTimerService {
      override fun cancel() {
        bgCancelled = true
      }
    })
    TestCase.assertFalse(bgCancelled)

    viewmodel.cancelCountdown()
    TestCase.assertTrue(bgCancelled)
  }
}