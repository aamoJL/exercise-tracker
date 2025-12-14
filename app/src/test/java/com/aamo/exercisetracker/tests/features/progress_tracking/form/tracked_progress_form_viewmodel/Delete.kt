package com.aamo.exercisetracker.tests.features.progress_tracking.form.tracked_progress_form_viewmodel

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.form.TrackedProgressFormViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Delete {
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `deleteData called`() = runTest(UnconfinedTestDispatcher()) {
    var called: TrackedProgress? = null
    val data = TrackedProgress(name = "Progress 1")
    val viewmodel = TrackedProgressFormViewModel(
      fetchData = { data },
      saveData = { fail() },
      deleteData = { called = it })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.formState.collect()
    }

    viewmodel.delete()
    assertEquals(data, called)
  }
}