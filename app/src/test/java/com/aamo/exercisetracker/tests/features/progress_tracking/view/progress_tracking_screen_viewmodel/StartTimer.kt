package com.aamo.exercisetracker.tests.features.progress_tracking.view.progress_tracking_screen_viewmodel

import com.aamo.exercisetracker.features.progress_tracking.view.ProgressTrackingScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.view.models.ProgressTrackingTrackedProgressModel
import com.aamo.exercisetracker.services.ICountdownTimerService
import com.aamo.exercisetracker.services.IStopwatchTimerService
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
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

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("HardCodedStringLiteral")
class StartTimer : UnconfinedTest() {
  @Test
  fun `countdown cancelled and started`() = runTest(UnconfinedTestDispatcher()) {
    var cancelled = false
    var started = false
    val model = ProgressTrackingTrackedProgressModel(
      id = 1L,
      name = "Progress 1",
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.COUNTDOWN,
      values = emptyList(),
      recordUnit = "Unit",
      countdownTime = 4.minutes
    )
    val viewmodel =
      ProgressTrackingScreenViewModel(fetchData = { flow { emit(model) } }, addValue = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    viewmodel.startCountdown(countdownTimerService = object : ICountdownTimerService {
      override fun start(
        durationMillis: Long,
        onFinished: () -> Unit,
        onStart: (() -> Unit)?,
        onCleanUp: (() -> Unit)?
      ) {
        assertTrue(cancelled)
        started = true
      }

      override fun cancel() {
        assertFalse(started)
        cancelled = true
      }
    })
    assertTrue(cancelled)
    assertTrue(started)
  }

  @Test
  fun `countdown state changes on service callbacks`() = runTest(UnconfinedTestDispatcher()) {
    val model = ProgressTrackingTrackedProgressModel(
      id = 1L,
      name = "Progress 1",
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.COUNTDOWN,
      values = emptyList(),
      recordUnit = "Unit",
      countdownTime = 4.minutes
    )
    val viewmodel =
      ProgressTrackingScreenViewModel(fetchData = { flow { emit(model) } }, addValue = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    val countDownService = object : ICountdownTimerService {
      override fun start(
        durationMillis: Long,
        onFinished: () -> Unit,
        onStart: (() -> Unit)?,
        onCleanUp: (() -> Unit)?
      ) {
        assertEquals(viewmodel.countdownTimerState.duration.inWholeMilliseconds, durationMillis)

        onStart!!.invoke().also { assertTrue(viewmodel.countdownTimerState.isActive.value) }
        onCleanUp!!.invoke().also { assertFalse(viewmodel.countdownTimerState.isActive.value) }
      }
    }

    viewmodel.startCountdown(countdownTimerService = countDownService)
  }

  @Test
  fun `stopwatch cancelled and started`() = runTest(UnconfinedTestDispatcher()) {
    var cancelled = false
    var started = false
    val model = ProgressTrackingTrackedProgressModel(
      id = 1L,
      name = "Progress 1",
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.STOPWATCH,
      values = emptyList(),
      recordUnit = "Unit",
      countdownTime = null
    )
    val viewmodel =
      ProgressTrackingScreenViewModel(fetchData = { flow { emit(model) } }, addValue = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    viewmodel.startStopwatch(stopwatchTimerService = object : IStopwatchTimerService {
      override fun start(
        onStart: (() -> Unit)?, onFinished: (Duration) -> Unit, onCleanUp: (() -> Unit)?
      ) {
        assertTrue(cancelled)
        started = true
      }

      override fun cancel() {
        assertFalse(started)
        cancelled = true
      }
    })
    assertTrue(cancelled)
    assertTrue(started)
  }

  @Test
  fun `stopwatch state changes on service callbacks`() = runTest(UnconfinedTestDispatcher()) {
    val model = ProgressTrackingTrackedProgressModel(
      id = 1L,
      name = "Progress 1",
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.STOPWATCH,
      values = emptyList(),
      recordUnit = "Unit",
      countdownTime = null
    )
    val viewmodel =
      ProgressTrackingScreenViewModel(fetchData = { flow { emit(model) } }, addValue = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    val stopwatchService = object : IStopwatchTimerService {
      override fun start(
        onStart: (() -> Unit)?, onFinished: (Duration) -> Unit, onCleanUp: (() -> Unit)?
      ) {
        val finalDuration = 3.minutes

        onStart!!.invoke().also { assertTrue(viewmodel.stopwatchTimerState.isActive.value) }
        onFinished.invoke(finalDuration)
          .also { assertEquals(finalDuration, viewmodel.stopwatchTimerState.finalDuration.value) }
        onCleanUp!!.invoke().also { assertFalse(viewmodel.stopwatchTimerState.isActive.value) }
      }
    }

    viewmodel.startStopwatch(stopwatchTimerService = stopwatchService)
  }
}