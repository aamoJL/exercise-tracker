package com.aamo.exercisetracker.tests.features.progress_tracking.view.progress_tracking_screen_viewmodel.countdown

import com.aamo.exercisetracker.features.progress_tracking.view.ProgressTrackingScreenViewModel
import junit.framework.TestCase
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class CountDownTimerState {
  @Test
  fun `isActive transformation`() {
    val state = ProgressTrackingScreenViewModel.CountdownTimerState()

    TestCase.assertEquals(0.seconds, state.duration)
    TestCase.assertFalse(state.isActive.value)

    state.isActive.update(true) // Should not change to true because duration is 0 seconds
    TestCase.assertFalse(state.isActive.value)

    state.duration = 1.seconds
    TestCase.assertFalse(state.isActive.value)

    state.isActive.update(true) // Should change to true because duration is over 0 seconds
    TestCase.assertTrue(state.isActive.value)
  }
}