package com.aamo.exercisetracker.tests.features.progress_tracking.form.tracked_progress_form_viewmodel

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.form.TrackedProgressFormViewModel
import com.aamo.exercisetracker.features.progress_tracking.form.models.TrackedProgressFormFields
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@Suppress("HardCodedStringLiteral")
class Save : UnconfinedTest() {
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `saveData called`() = runTest(UnconfinedTestDispatcher()) {
    var called: TrackedProgress? = null
    val data = TrackedProgress(id = 1L, name = "Progress 1")
    val viewmodel = TrackedProgressFormViewModel(
      fetchData = { data },
      saveData = { called = it },
      deleteData = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.formState.collect()
    }

    val newName = "New name"
    val newInterval = 2
    val newUnit = "New Unit"
    viewmodel.formState.value!!.progressName.update(newName)
    viewmodel.formState.value!!.weeklyInterval.update(newInterval)
    viewmodel.formState.value!!.progressValueUnit.update(newUnit)
    viewmodel.formState.value!!.progressType.update(TrackedProgressFormFields.ProgressType.STOPWATCH)

    val expected = data.copy(
      name = newName,
      intervalWeeks = newInterval,
      unit = newUnit,
      hasStopWatch = true,
      timerTime = null
    )

    viewmodel.save()
    assertEquals(expected, called)
  }
}