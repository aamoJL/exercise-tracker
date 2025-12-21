package com.aamo.exercisetracker.tests.features.exercise.form.exercise_form_viewmodel

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.features.exercise.form.ExerciseFormViewModel
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
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
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("HardCodedStringLiteral")
class Init : UnconfinedTest() {
  @Test
  fun `is new`() = runTest(UnconfinedTestDispatcher()) {
    val data = ExerciseWithSets(exercise = Exercise(routineId = 1L), sets = emptyList())
    val viewmodel =
      ExerciseFormViewModel(fetchData = { data }, saveData = { fail() }, deleteData = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.formState.collect()
    }

    val uiState = checkNotNull(viewmodel.formState.value)
    assertTrue(uiState.isNew)
  }

  @Test
  fun `is not new`() = runTest(UnconfinedTestDispatcher()) {
    val data = ExerciseWithSets(
      exercise = Exercise(id = 1L, routineId = 1L, name = "Exercise 1"), sets = emptyList()
    )
    val viewmodel =
      ExerciseFormViewModel(fetchData = { data }, saveData = { fail() }, deleteData = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.formState.collect()
    }

    val uiState = checkNotNull(viewmodel.formState.value)
    assertFalse(uiState.isNew)
  }

  @Test
  fun `form state set`() = runTest(UnconfinedTestDispatcher()) {
    val exercise = Exercise(id = 1L, routineId = 1L, name = "Exercise 1", restDuration = 3.minutes)
    val set = ExerciseSet(
      id = 1L,
      exerciseId = exercise.id,
      value = 123,
      unit = "Unit",
      valueType = ExerciseSet.ValueType.COUNTDOWN
    )
    val data = ExerciseWithSets(exercise = exercise, sets = listOf(set))

    val viewmodel =
      ExerciseFormViewModel(fetchData = { data }, saveData = { fail() }, deleteData = { fail() })


    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.formState.collect()
    }

    val formState = checkNotNull(viewmodel.formState.value)
    assertEquals(data.exercise.name, formState.exerciseName.value)
    assertEquals(data.exercise.restDuration, formState.restDuration.value)
    assertEquals(data.sets.first().unit, formState.setUnit.value)
    assertEquals(data.sets.map { it.value }, formState.setValues.values.map { it.value.value })
    assertEquals(
      data.sets.first().valueType == ExerciseSet.ValueType.COUNTDOWN, formState.hasTimer.value
    )
    assertEquals(data.exercise.restDuration > 0.seconds, formState.hasRest.value)
  }
}