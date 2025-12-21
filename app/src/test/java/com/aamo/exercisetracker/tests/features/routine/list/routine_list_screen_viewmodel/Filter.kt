package com.aamo.exercisetracker.tests.features.routine.list.routine_list_screen_viewmodel

import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.features.routine.list.RoutineListScreenViewModel
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@Suppress("HardCodedStringLiteral")
@OptIn(ExperimentalCoroutinesApi::class)
class Filter : UnconfinedTest() {
  @Test
  fun filter() = runTest(UnconfinedTestDispatcher()) {
    val dataFlow = MutableSharedFlow<List<RoutineWithSchedule>>()
    val viewmodel = RoutineListScreenViewModel(fetchData = { dataFlow }, deleteData = {})

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.filteredRoutines.collect()
    }

    val routines = listOf(
      RoutineWithSchedule(routine = Routine(name = "A"), schedule = null),
      RoutineWithSchedule(routine = Routine(name = "A"), schedule = null),
      RoutineWithSchedule(routine = Routine(name = "B"), schedule = null),
      RoutineWithSchedule(routine = Routine(name = "C"), schedule = null),
    ).also {
      dataFlow.emit(it)
    }

    TestCase.assertEquals(routines, viewmodel.filteredRoutines.value)

    val filterWord = routines.first().routine.name
    viewmodel.setFilterWord(filterWord)

    TestCase.assertEquals(filterWord, viewmodel.filterWord.value)
    TestCase.assertEquals(
      routines.filter { it.routine.name == filterWord }, viewmodel.filteredRoutines.value
    )
  }
}