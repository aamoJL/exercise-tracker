package com.aamo.exercisetracker.tests.features.exercise.view.exercise_screen_viewmodel

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.features.exercise.view.ExerciseScreenViewModel
import com.aamo.exercisetracker.services.ICountdownTimerService
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import com.aamo.exercisetracker.utility.viewmodels.ITimer
import junit.framework.TestCase
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class StartSet : UnconfinedTest() {
  @Test
  fun `set changed if set has no timer`() = runTest(UnconfinedTestDispatcher()) {
    var bgStarted = false
    val data = ExerciseWithSets(
      exercise = Exercise(routineId = 0L), sets = listOf(
        ExerciseSet(exerciseId = 0L, value = 123, valueType = ExerciseSet.ValueType.REPETITION),
        ExerciseSet(exerciseId = 0L, value = 123, valueType = ExerciseSet.ValueType.REPETITION),
      )
    )
    val viewmodel = ExerciseScreenViewModel(fetchData = { data }, saveProgress = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    viewmodel.startSet(backgroundService = object : ICountdownTimerService {
      override fun start(duration: Duration) {
        bgStarted = true
      }
    })

    assertFalse(bgStarted)
    assertEquals(1, viewmodel.uiState.value?.currentSet?.value?.index)
    assertEquals(data.sets[1], viewmodel.uiState.value?.currentSet?.value?.set)
  }

  @Test
  fun `timer started if set has timer`() = runTest(UnconfinedTestDispatcher()) {
    var timerStarted = false
    val data = ExerciseWithSets(
      exercise = Exercise(routineId = 0L), sets = listOf(
        ExerciseSet(exerciseId = 0L, value = 123, valueType = ExerciseSet.ValueType.COUNTDOWN)
      )
    )
    val viewmodel = ExerciseScreenViewModel(
      fetchData = { data },
      saveProgress = { fail() },
      timer = object : ITimer {
        override fun start(duration: Duration, onFinished: () -> Unit) {
          timerStarted = true
        }
      })


    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    viewmodel.startSet()

    assertTrue(timerStarted)
    assertEquals(0, viewmodel.uiState.value?.currentSet?.value?.index)
    assertEquals(
      true, viewmodel.uiState.value?.currentSet?.value?.timerState?.isActive?.value
    )
  }

  @Test
  fun `set changes when set timer finishes`() = runTest(UnconfinedTestDispatcher()) {
    var finishRest: (() -> Unit)? = null
    val data = ExerciseWithSets(
      exercise = Exercise(routineId = 0L), sets = listOf(
        ExerciseSet(exerciseId = 0L, value = 3, valueType = ExerciseSet.ValueType.COUNTDOWN),
        ExerciseSet(exerciseId = 0L, value = 3, valueType = ExerciseSet.ValueType.COUNTDOWN),
      )
    )
    val viewmodel = ExerciseScreenViewModel(
      fetchData = { data },
      saveProgress = { fail() },
      timer = object : ITimer {
        override fun start(duration: Duration, onFinished: () -> Unit) {
          finishRest = onFinished
        }
      })


    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    assertEquals(0, viewmodel.uiState.value?.currentSet?.value?.index)
    viewmodel.startSet()
    assertEquals(0, viewmodel.uiState.value?.currentSet?.value?.index)
    finishRest?.invoke()
    assertEquals(1, viewmodel.uiState.value?.currentSet?.value?.index)
  }

  @Test
  fun `set changes when rest timer finishes`() = runTest(UnconfinedTestDispatcher()) {
    var finishRest: (() -> Unit)? = null
    val data = ExerciseWithSets(
      exercise = Exercise(routineId = 0L, restDuration = 3.minutes), sets = listOf(
        ExerciseSet(exerciseId = 0L, value = 3, valueType = ExerciseSet.ValueType.REPETITION),
        ExerciseSet(exerciseId = 0L, value = 3, valueType = ExerciseSet.ValueType.REPETITION),
      )
    )
    val viewmodel = ExerciseScreenViewModel(
      fetchData = { data },
      saveProgress = { fail() },
      timer = object : ITimer {
        override fun start(duration: Duration, onFinished: () -> Unit) {
          finishRest = onFinished
        }
      })


    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    assertEquals(0, viewmodel.uiState.value?.currentSet?.value?.index)
    viewmodel.startSet()
    assertEquals(0, viewmodel.uiState.value?.currentSet?.value?.index)
    finishRest?.invoke()
    assertEquals(1, viewmodel.uiState.value?.currentSet?.value?.index)
  }

  @Test
  fun `rest timer starts after set timer`() = runTest(UnconfinedTestDispatcher()) {
    var restStarted = false
    var setStarted = false
    val data = ExerciseWithSets(
      exercise = Exercise(routineId = 0L, restDuration = 1.minutes), sets = listOf(
        ExerciseSet(exerciseId = 0L, value = 1, valueType = ExerciseSet.ValueType.COUNTDOWN),
        ExerciseSet(exerciseId = 0L, value = 2, valueType = ExerciseSet.ValueType.COUNTDOWN)
      )
    )
    val viewmodel = ExerciseScreenViewModel(
      fetchData = { data },
      saveProgress = { fail() },
      timer = object : ITimer {
        override fun start(duration: Duration, onFinished: () -> Unit) {
          if (duration == data.exercise.restDuration) {
            if (!setStarted) fail()
            restStarted = true
          }
          if (duration == data.sets.first().value.milliseconds) {
            if (restStarted) fail()
            setStarted = true
            onFinished() // don't finish rest timer
          }
        }
      })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    viewmodel.startSet()

    assertTrue(setStarted)
    assertTrue(restStarted)
    assertEquals(true, viewmodel.uiState.value?.restTimerState?.isActive?.value)
  }

  @Test
  fun `set is null when last set finishes`() = runTest(UnconfinedTestDispatcher()) {
    val data = ExerciseWithSets(
      exercise = Exercise(routineId = 0L), sets = listOf(
        ExerciseSet(exerciseId = 0L, value = 1, valueType = ExerciseSet.ValueType.REPETITION)
      )
    )
    val viewmodel = ExerciseScreenViewModel(fetchData = { data }, saveProgress = { fail() })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    viewmodel.startSet()

    TestCase.assertNull(viewmodel.uiState.value!!.currentSet.value.set)
  }

  @Test
  fun `rest timer does not start if the set was the last one`() =
    runTest(UnconfinedTestDispatcher()) {
      var called = false
      val data = ExerciseWithSets(
        exercise = Exercise(routineId = 0L, restDuration = 3.minutes), sets = listOf(
          ExerciseSet(exerciseId = 0L, value = 3, valueType = ExerciseSet.ValueType.REPETITION),
        )
      )
      val viewmodel = ExerciseScreenViewModel(
        fetchData = { data },
        saveProgress = { fail() },
        timer = object : ITimer {
          override fun start(duration: Duration, onFinished: () -> Unit) {
            called = true
          }
        })


      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        viewmodel.uiState.collect()
      }

      assertFalse(called)
      viewmodel.startSet()
      assertFalse(called)
    }
}