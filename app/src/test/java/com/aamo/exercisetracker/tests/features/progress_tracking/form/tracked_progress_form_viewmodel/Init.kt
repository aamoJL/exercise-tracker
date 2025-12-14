package com.aamo.exercisetracker.tests.features.progress_tracking.form.tracked_progress_form_viewmodel

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.form.TrackedProgressFormViewModel
import com.aamo.exercisetracker.features.progress_tracking.form.models.TrackedProgressFormFields
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Init {
  @Test
  fun `is new`() = runTest(UnconfinedTestDispatcher()) {
    val data = TrackedProgress()
    val viewmodel = TrackedProgressFormViewModel(
      fetchData = { data },
      saveData = { fail() },
      deleteData = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.formState.collect()
    }

    val uiState = checkNotNull(viewmodel.formState.value)
    assertTrue(uiState.isNew)
  }

  @Test
  fun `is not new`() = runTest(UnconfinedTestDispatcher()) {
    val data = TrackedProgress(id = 1L, name = "Progress 1")
    val viewmodel = TrackedProgressFormViewModel(
      fetchData = { data },
      saveData = { fail() },
      deleteData = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.formState.collect()
    }

    val uiState = checkNotNull(viewmodel.formState.value)
    assertFalse(uiState.isNew)
  }

  @Test
  fun `form state set`() = runTest(UnconfinedTestDispatcher()) {
    val data = TrackedProgress(
      id = 1L,
      name = "Progress 1",
      intervalWeeks = 2,
      unit = "Unit",
      hasStopWatch = true,
      timerTime = 4.minutes.inWholeMilliseconds
    )
    val viewmodel = TrackedProgressFormViewModel(
      fetchData = { data },
      saveData = { fail() },
      deleteData = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.formState.collect()
    }

    val uiState = checkNotNull(viewmodel.formState.value)
    assertEquals(data.name, uiState.progressName.value)
    assertEquals(data.intervalWeeks, uiState.weeklyInterval.value)
    assertEquals(data.unit, uiState.progressValueUnit.value)
    assertEquals(TrackedProgressFormFields.ProgressType.STOPWATCH, uiState.progressType.value)
    assertEquals(data.timerTime!!.milliseconds, uiState.timerDuration.value)
  }
}