package com.aamo.exercisetracker.tests.features.routine.form.routine_form_viewmodel

import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.features.routine.form.RoutineFormViewModel
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import com.aamo.exercisetracker.utility.extensions.date.Day
import junit.framework.TestCase
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
    var called: RoutineWithSchedule? = null
    val data = RoutineWithSchedule(
      routine = Routine(id = 1L, name = "Routine 1"),
      schedule = RoutineSchedule(routineId = 1L, sunday = true, wednesday = true)
    )
    val viewmodel = RoutineFormViewModel(
      fetchData = { data },
      saveData = { called = it },
      deleteData = { TestCase.fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.formState.collect()
    }

    val newName = "New name"
    checkNotNull(viewmodel.formState.value).also {
      it.routineName.update(newName)
      it.selectedDays.update(it.selectedDays.value.plus(Day.SATURDAY))
    }

    val expected = data.copy(
      routine = data.routine.copy(name = newName), schedule = data.schedule!!.copy(saturday = true)
    )

    viewmodel.save()
    TestCase.assertEquals(expected, called)
  }
}