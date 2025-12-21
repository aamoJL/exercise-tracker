package com.aamo.exercisetracker.tests.features.exercise.form.exercise_form_viewmodel

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.features.exercise.form.ExerciseFormViewModel
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
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
    var called: ExerciseWithSets? = null
    val data = ExerciseWithSets(
      exercise = Exercise(id = 1L, routineId = 1L, name = "Exercise 1"), sets = listOf(
        ExerciseSet(id = 1L, exerciseId = 1L, value = 123, unit = "Unit")
      )
    )
    val viewmodel = ExerciseFormViewModel(
      fetchData = { data },
      saveData = { called = it },
      deleteData = { TestCase.fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.formState.collect()
    }

    val newName = "New name"
    val newValue = 1
    checkNotNull(viewmodel.formState.value).apply {
      this.exerciseName.update(newName)
      this.setValues.values.first().value.update(newValue)
    }

    val expected = data.copy(
      exercise = data.exercise.copy(name = newName), sets = data.sets.let {
        it.toMutableList().apply { this[0] = this[0].copy(id = 0L, value = newValue) }
      })

    viewmodel.save()
    TestCase.assertEquals(expected, called)
  }
}