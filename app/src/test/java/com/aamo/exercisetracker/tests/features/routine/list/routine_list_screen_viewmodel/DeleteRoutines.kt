package com.aamo.exercisetracker.tests.features.routine.list.routine_list_screen_viewmodel

import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.features.routine.list.RoutineListScreenViewModel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class DeleteRoutines {
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `deleteData called`() = runTest(UnconfinedTestDispatcher()) {
    var called: List<Routine>? = null
    val viewmodel =
      RoutineListScreenViewModel(fetchData = { flow { } }, deleteData = { called = it })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.filteredRoutines.collect()
    }

    val routines = listOf(
      Routine(name = "A"),
      Routine(name = "B"),
    )

    viewmodel.deleteRoutines(routines)

    assertEquals(routines, called)
  }
}