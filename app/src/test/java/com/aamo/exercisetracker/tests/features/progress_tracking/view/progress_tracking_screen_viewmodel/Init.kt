package com.aamo.exercisetracker.tests.features.progress_tracking.view.progress_tracking_screen_viewmodel

import com.aamo.exercisetracker.features.progress_tracking.view.ProgressTrackingScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.view.models.ProgressTrackingTrackedProgressModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.seconds

@Suppress("HardCodedStringLiteral")
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class Init {
  @Test
  fun `model set`() = runTest(UnconfinedTestDispatcher()) {
    val dataFlow = MutableSharedFlow<ProgressTrackingTrackedProgressModel>()
    val viewmodel = ProgressTrackingScreenViewModel(fetchData = { dataFlow }, addValue = {})

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    assertNull(viewmodel.model.value)

    val model = ProgressTrackingTrackedProgressModel(
      id = 1L,
      name = "Progress 1",
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.REPETITION,
      records = emptyList(),
      recordUnit = "Unit",
      countDownTime = null
    )

    dataFlow.emit(model)
    assertEquals(model, viewmodel.model.value)
  }

  @Test
  fun `countDownTimerState set`() = runTest(UnconfinedTestDispatcher()) {
    val dataFlow = MutableSharedFlow<ProgressTrackingTrackedProgressModel>()
    val viewmodel = ProgressTrackingScreenViewModel(fetchData = { dataFlow }, addValue = {})

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    assertEquals(0.seconds, viewmodel.countDownTimerState.duration)
    assertFalse(viewmodel.countDownTimerState.isActive.value)

    val model = ProgressTrackingTrackedProgressModel(
      id = 1L,
      name = "Progress 1",
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.COUNTDOWN,
      records = emptyList(),
      recordUnit = "Unit",
      countDownTime = 4.seconds
    )

    dataFlow.emit(model)

    checkNotNull(viewmodel.model.value)
    assertEquals(model.countDownTime!!, viewmodel.countDownTimerState.duration)
    assertFalse(viewmodel.countDownTimerState.isActive.value)
  }

  @Test
  fun `stopwatchTimerState set`() = runTest(UnconfinedTestDispatcher()) {
    val dataFlow = MutableSharedFlow<ProgressTrackingTrackedProgressModel>()
    val viewmodel = ProgressTrackingScreenViewModel(fetchData = { dataFlow }, addValue = {})

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    assertEquals(0.seconds, viewmodel.stopwatchTimerState.finalDuration.value)
    assertFalse(viewmodel.stopwatchTimerState.isActive.value)

    val model = ProgressTrackingTrackedProgressModel(
      id = 1L,
      name = "Progress 1",
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.COUNTDOWN,
      records = emptyList(),
      recordUnit = "Unit",
      countDownTime = 4.seconds
    )

    dataFlow.emit(model)

    checkNotNull(viewmodel.model.value)
    assertEquals(0.seconds, viewmodel.stopwatchTimerState.finalDuration.value)
    assertFalse(viewmodel.stopwatchTimerState.isActive.value)
  }
}