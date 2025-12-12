package com.aamo.exercisetracker.tests.features.routine.list.routine_list_screen_viewmodel

import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.features.routine.list.RoutineListScreenViewModel
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Init {
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `filtered routines set`() = runTest(UnconfinedTestDispatcher()) {
    val dataFlow = MutableSharedFlow<List<RoutineWithSchedule>>()
    val viewmodel = RoutineListScreenViewModel(fetchData = { dataFlow }, deleteData = {})

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.filteredRoutines.collect()
    }

    assertNull(viewmodel.filteredRoutines.value)

    val routines = listOf(
      RoutineWithSchedule(routine = Routine(name = "Routine 1"), schedule = null)
    ).also {
      dataFlow.emit(it)
    }

    assertEquals(routines, viewmodel.filteredRoutines.value)
  }

  @Test
  fun `filter word is empty`() {
    val viewmodel = RoutineListScreenViewModel(fetchData = { flow { } }, deleteData = {})

    assertEquals(String.EMPTY, viewmodel.filterWord.value)
  }

  @Test
  fun `selections is empty`() {
    val viewmodel = RoutineListScreenViewModel(fetchData = { flow { } }, deleteData = {})

    assertTrue(viewmodel.selections.value.isEmpty())
  }
}