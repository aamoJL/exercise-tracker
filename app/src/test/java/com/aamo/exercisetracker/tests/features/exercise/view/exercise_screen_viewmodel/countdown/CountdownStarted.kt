package com.aamo.exercisetracker.tests.features.exercise.view.exercise_screen_viewmodel.countdown

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.features.exercise.view.ExerciseScreenViewModel
import com.aamo.exercisetracker.services.ICountdownTimerService
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import com.aamo.exercisetracker.utility.viewmodels.ITimer
import junit.framework.TestCase
import junit.framework.TestCase.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class CountdownStarted : UnconfinedTest() {
  @Test
  fun `countdown started`() = runTest(UnconfinedTestDispatcher()) {
    var countdownStarted = false
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
          countdownStarted = true
        }
      })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    TestCase.assertFalse(countdownStarted)
    TestCase.assertEquals(
      false, viewmodel.uiState.value?.currentSet?.value?.timerState?.isActive?.value
    )

    viewmodel.startSet()
    TestCase.assertTrue(countdownStarted)
    TestCase.assertEquals(
      true, viewmodel.uiState.value?.currentSet?.value?.timerState?.isActive?.value
    )
  }

  @Test
  fun `background service started`() = runTest(UnconfinedTestDispatcher()) {
    var bgStarted = false
    val data = ExerciseWithSets(
      exercise = Exercise(routineId = 0L), sets = listOf(
        ExerciseSet(exerciseId = 0L, value = 123, valueType = ExerciseSet.ValueType.COUNTDOWN)
      )
    )
    val viewmodel = ExerciseScreenViewModel(
      fetchData = { data },
      saveProgress = { fail() },
      timer = object : ITimer {})

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    TestCase.assertFalse(bgStarted)

    viewmodel.startSet(backgroundService = object : ICountdownTimerService {
      override fun start(duration: Duration) {
        bgStarted = true
      }
    })
    TestCase.assertTrue(bgStarted)
  }

  @Test
  fun `countdown duration is correct`() = runTest(UnconfinedTestDispatcher()) {
    var actual: Duration? = null
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
          actual = duration
        }
      })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    TestCase.assertNull(actual)
    viewmodel.startSet(backgroundService = object : ICountdownTimerService {
      override fun start(duration: Duration) {
        if (actual == null || actual != duration) fail()
      }
    })
    TestCase.assertEquals(data.sets.first().value.milliseconds, actual!!)
  }
}