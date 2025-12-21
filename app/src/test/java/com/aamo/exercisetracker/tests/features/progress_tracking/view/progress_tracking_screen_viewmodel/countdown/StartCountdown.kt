package com.aamo.exercisetracker.tests.features.progress_tracking.view.progress_tracking_screen_viewmodel.countdown

import com.aamo.exercisetracker.features.progress_tracking.view.ProgressTrackingScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.view.models.ProgressTrackingTrackedProgressModel
import com.aamo.exercisetracker.services.ICountdownTimerService
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import com.aamo.exercisetracker.utility.viewmodels.ITimer
import junit.framework.TestCase
import junit.framework.TestCase.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
@OptIn(ExperimentalCoroutinesApi::class)
class StartCountdown : UnconfinedTest() {
  @Test
  fun `countdown started`() = runTest(UnconfinedTestDispatcher()) {
    var countdownStarted = false
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
      addValue = { fail() },
      timer = object : ITimer {
        override fun start(duration: Duration, onFinished: () -> Unit) {
          countdownStarted = true
        }
      })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    TestCase.assertFalse(countdownStarted)
    TestCase.assertFalse(viewmodel.countdownTimerState.isActive.value)

    viewmodel.startCountdown()
    TestCase.assertTrue(countdownStarted)
    TestCase.assertTrue(viewmodel.countdownTimerState.isActive.value)
  }

  @Test
  fun `background service started`() = runTest(UnconfinedTestDispatcher()) {
    var bgStarted = false
    val data = ProgressTrackingTrackedProgressModel(
      id = 1L,
      name = "Exercise 1",
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.COUNTDOWN,
      values = emptyList(),
      recordUnit = "Unit",
      countdownTime = 3.minutes
    )
    val viewmodel =
      ProgressTrackingScreenViewModel(fetchData = { flow { emit(data) } }, addValue = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    TestCase.assertFalse(bgStarted)

    viewmodel.startCountdown(backgroundService = object : ICountdownTimerService {
      override fun start(duration: Duration) {
        bgStarted = true
      }
    })
    TestCase.assertTrue(bgStarted)
  }

  @Test
  fun `countdown duration is correct`() = runTest(UnconfinedTestDispatcher()) {
    var actual: Duration? = null
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
      addValue = { fail() },
      timer = object : ITimer {
        override fun start(duration: Duration, onFinished: () -> Unit) {
          actual = duration
        }
      })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    TestCase.assertNull(actual)
    viewmodel.startCountdown(backgroundService = object : ICountdownTimerService {
      override fun start(duration: Duration) {
        if (actual == null || actual != duration) fail()
      }
    })
    TestCase.assertEquals(data.countdownTime, actual)
  }
}