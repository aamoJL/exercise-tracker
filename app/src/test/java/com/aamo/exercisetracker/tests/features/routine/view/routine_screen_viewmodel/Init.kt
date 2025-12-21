package com.aamo.exercisetracker.tests.features.routine.view.routine_screen_viewmodel

import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineWithExerciseProgresses
import com.aamo.exercisetracker.features.routine.view.RoutineScreenViewModel
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@Suppress("HardCodedStringLiteral")
class Init : UnconfinedTest() {
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `model set`() = runTest(UnconfinedTestDispatcher()) {
    val dataFlow = MutableSharedFlow<RoutineWithExerciseProgresses?>()
    val viewmodel = RoutineScreenViewModel(fetchData = { dataFlow })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    assertNull(viewmodel.model.value)

    val model = RoutineWithExerciseProgresses(
      routine = Routine(name = "Routine 1"), exerciseProgresses = emptyList()
    ).also {
      dataFlow.emit(it)
    }

    assertEquals(model, viewmodel.model.value)
  }
}