package com.aamo.exercisetracker.tests.features.progress_tracking.view.progress_tracking_screen_viewmodel

import com.aamo.exercisetracker.features.progress_tracking.view.ProgressTrackingScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.view.models.ProgressTrackingTrackedProgressModel
import com.aamo.exercisetracker.services.ICountDownTimerService
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
      records = emptyList(),
      recordUnit = "Unit",
      countDownTime = 4.minutes
    )
    val viewmodel =
      ProgressTrackingScreenViewModel(fetchData = { flow { emit(model) } }, addValue = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    viewmodel.startTimer(countDownTimerService = object : ICountDownTimerService {
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
      records = emptyList(),
      recordUnit = "Unit",
      countDownTime = 4.minutes
    )
    val viewmodel =
      ProgressTrackingScreenViewModel(fetchData = { flow { emit(model) } }, addValue = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    val countDownService = object : ICountDownTimerService {
      override fun start(
        durationMillis: Long,
        onFinished: () -> Unit,
        onStart: (() -> Unit)?,
        onCleanUp: (() -> Unit)?
      ) {
        assertEquals(viewmodel.countDownTimerState.duration.inWholeMilliseconds, durationMillis)

        onStart!!.invoke().also { assertTrue(viewmodel.countDownTimerState.isActive.value) }
        onCleanUp!!.invoke().also { assertFalse(viewmodel.countDownTimerState.isActive.value) }
      }
    }

    viewmodel.startTimer(countDownTimerService = countDownService)
  }

  @Test
  fun `stopwatch cancelled and started`() = runTest(UnconfinedTestDispatcher()) {
    var cancelled = false
    var started = false
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

    viewmodel.startTimer(stopwatchTimerService = object : IStopwatchTimerService {
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
      records = emptyList(),
      recordUnit = "Unit",
      countDownTime = null
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

    viewmodel.startTimer(stopwatchTimerService = stopwatchService)
  }
}