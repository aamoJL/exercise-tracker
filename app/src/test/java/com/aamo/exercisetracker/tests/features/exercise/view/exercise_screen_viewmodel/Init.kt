package com.aamo.exercisetracker.tests.features.exercise.view.exercise_screen_viewmodel

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.features.exercise.view.ExerciseScreenViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class Init {
  @Test
  fun `model set`() = runTest(UnconfinedTestDispatcher()) {
    val data = ExerciseWithSets(exercise = Exercise(routineId = 0L), sets = emptyList())
    val viewmodel = ExerciseScreenViewModel(fetchData = { data }, saveProgress = { fail() })

    assertNull(viewmodel.model)

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    assertEquals(data.exercise, viewmodel.model)
  }

  @Test
  fun `UI state name set`() = runTest(UnconfinedTestDispatcher()) {
    val data = ExerciseWithSets(
      exercise = Exercise(routineId = 0L, name = "Exercise 1"), sets = emptyList()
    )
    val viewmodel = ExerciseScreenViewModel(fetchData = { data }, saveProgress = { fail() })

    assertNull(viewmodel.uiState.value)

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    checkNotNull(viewmodel.uiState.value).also { ui ->
      assertEquals(data.exercise.name, ui.exerciseName)
    }
  }

  @Test
  fun `UI state set without rest timer`() = runTest(UnconfinedTestDispatcher()) {
    val data = ExerciseWithSets(
      exercise = Exercise(routineId = 0L, name = "Exercise 1"), sets = emptyList()
    )
    val viewmodel = ExerciseScreenViewModel(fetchData = { data }, saveProgress = { fail() })

    assertNull(viewmodel.uiState.value)

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    checkNotNull(viewmodel.uiState.value).also { ui ->
      assertNull(ui.currentSet.value.timerState)
      assertNull(ui.restTimerState)
    }
  }

  @Test
  fun `UI state set with rest timer`() = runTest(UnconfinedTestDispatcher()) {
    val data = ExerciseWithSets(
      exercise = Exercise(routineId = 0L, name = "Exercise 1", restDuration = 3.minutes),
      sets = listOf(
        ExerciseSet(exerciseId = 0L, value = 123, valueType = ExerciseSet.ValueType.REPETITION)
      )
    )
    val viewmodel = ExerciseScreenViewModel(fetchData = { data }, saveProgress = { fail() })

    assertNull(viewmodel.uiState.value)

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    checkNotNull(viewmodel.uiState.value).also { ui ->
      assertNotNull(ui.restTimerState)
      assertEquals(data.exercise.restDuration, ui.restTimerState!!.duration)
      assertFalse(ui.restTimerState.isActive.value)
    }
  }

  @Test
  fun `UI state set without set timer`() = runTest(UnconfinedTestDispatcher()) {
    val data = ExerciseWithSets(
      exercise = Exercise(routineId = 0L, name = "Exercise 1"), sets = listOf(
        ExerciseSet(exerciseId = 0L, value = 123, valueType = ExerciseSet.ValueType.REPETITION)
      )
    )
    val viewmodel = ExerciseScreenViewModel(fetchData = { data }, saveProgress = { fail() })

    assertNull(viewmodel.uiState.value)

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    checkNotNull(viewmodel.uiState.value).also { ui ->
      assertEquals(data.sets.first(), ui.currentSet.value.set)
      assertNull(ui.currentSet.value.timerState)
    }
  }

  @Test
  fun `UI state set with set timer`() = runTest(UnconfinedTestDispatcher()) {
    val data = ExerciseWithSets(
      exercise = Exercise(routineId = 0L, name = "Exercise 1"), sets = listOf(
        ExerciseSet(exerciseId = 0L, value = 3, valueType = ExerciseSet.ValueType.COUNTDOWN)
      )
    )
    val viewmodel = ExerciseScreenViewModel(fetchData = { data }, saveProgress = { fail() })

    assertNull(viewmodel.uiState.value)

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    checkNotNull(viewmodel.uiState.value).also { ui ->
      assertEquals(data.sets.first(), ui.currentSet.value.set)
      assertNotNull(ui.currentSet.value.timerState)
      assertEquals(data.sets.first().value.milliseconds, ui.currentSet.value.timerState!!.duration)
      assertFalse(ui.currentSet.value.timerState!!.isActive.value)
    }
  }
}