package com.aamo.exercisetracker.tests.features.routine.form.routine_form_viewmodel

import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.features.routine.form.RoutineFormViewModel
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

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Init {
  @Test
  fun `is new`() = runTest(UnconfinedTestDispatcher()) {
    val data = RoutineWithSchedule(routine = Routine(), schedule = null)
    val viewmodel =
      RoutineFormViewModel(fetchData = { data }, saveData = { fail() }, deleteData = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.formState.collect()
    }

    val uiState = checkNotNull(viewmodel.formState.value)
    assertTrue(uiState.isNew)
  }

  @Test
  fun `is not new`() = runTest(UnconfinedTestDispatcher()) {
    val data = RoutineWithSchedule(routine = Routine(name = "Routine 1"), schedule = null)
    val viewmodel =
      RoutineFormViewModel(fetchData = { data }, saveData = { fail() }, deleteData = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.formState.collect()
    }

    val uiState = checkNotNull(viewmodel.formState.value)
    assertFalse(uiState.isNew)
  }

  @Test
  fun `form state set`() = runTest(UnconfinedTestDispatcher()) {
    val data = RoutineWithSchedule(
      routine = Routine(name = "Routine 1"),
      schedule = RoutineSchedule(routineId = 1L, sunday = true, wednesday = true)
    )
    val viewmodel =
      RoutineFormViewModel(fetchData = { data }, saveData = { fail() }, deleteData = { fail() })


    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.formState.collect()
    }

    val formState = checkNotNull(viewmodel.formState.value)
    assertEquals(data.routine.name, formState.routineName.value)
    assertEquals(data.schedule!!.asListOfDays(), formState.selectedDays.value)
  }
}