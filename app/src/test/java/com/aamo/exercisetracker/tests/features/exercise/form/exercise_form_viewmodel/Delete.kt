package com.aamo.exercisetracker.tests.features.exercise.form.exercise_form_viewmodel

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.features.exercise.form.ExerciseFormViewModel
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@Suppress("HardCodedStringLiteral")
class Delete : UnconfinedTest() {
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `deleteData called`() = runTest(UnconfinedTestDispatcher()) {
    var called: Exercise? = null
    val data = ExerciseWithSets(
      exercise = Exercise(routineId = 1L, name = "Exercise 1"), sets = emptyList()
    )
    val viewmodel = ExerciseFormViewModel(
      fetchData = { data },
      saveData = { TestCase.fail() },
      deleteData = { called = it })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.formState.collect()
    }

    viewmodel.delete()
    assertEquals(data.exercise, called)
  }
}