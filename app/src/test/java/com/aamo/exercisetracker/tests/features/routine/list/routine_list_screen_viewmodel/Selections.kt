package com.aamo.exercisetracker.tests.features.routine.list.routine_list_screen_viewmodel

import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.features.routine.list.RoutineListScreenViewModel
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@Suppress("HardCodedStringLiteral")
@OptIn(ExperimentalCoroutinesApi::class)
class Selections : UnconfinedTest() {
  @Test
  fun selections() = runTest(UnconfinedTestDispatcher()) {
    val dataFlow = MutableSharedFlow<List<RoutineWithSchedule>>()
    val viewmodel = RoutineListScreenViewModel(fetchData = { dataFlow }, deleteData = {})

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.selections.collect()
    }

    val routines = listOf(
      RoutineWithSchedule(routine = Routine(name = "A"), schedule = null),
      RoutineWithSchedule(routine = Routine(name = "B"), schedule = null),
      RoutineWithSchedule(routine = Routine(name = "C"), schedule = null),
      RoutineWithSchedule(routine = Routine(name = "D"), schedule = null),
    ).also {
      dataFlow.emit(it)
    }

    assertTrue(viewmodel.selections.value.isEmpty())

    // set true
    viewmodel.switchRoutineSelection(models = listOf(routines.first().routine), state = true)
    assertEquals(listOf(routines.first().routine), viewmodel.selections.value)

    // set true on the same item
    viewmodel.switchRoutineSelection(models = listOf(routines.first().routine), state = true)
    assertEquals(listOf(routines.first().routine), viewmodel.selections.value)

    // set false
    viewmodel.switchRoutineSelection(models = listOf(routines.first().routine), state = false)
    assertTrue(viewmodel.selections.value.isEmpty())
  }
}