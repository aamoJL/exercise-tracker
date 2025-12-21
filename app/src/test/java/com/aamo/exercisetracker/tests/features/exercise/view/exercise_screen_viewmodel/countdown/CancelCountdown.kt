package com.aamo.exercisetracker.tests.features.exercise.view.exercise_screen_viewmodel.countdown

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.features.exercise.view.ExerciseScreenViewModel
import com.aamo.exercisetracker.services.ICountdownTimerService
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import com.aamo.exercisetracker.utility.viewmodels.ITimer
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CancelCountdown : UnconfinedTest() {
  @Test
  fun `countdown stopped`() = runTest(UnconfinedTestDispatcher()) {
    var timerCleaned = false
    val data = ExerciseWithSets(
      exercise = Exercise(routineId = 0L), sets = listOf(
        ExerciseSet(exerciseId = 0L, value = 3, valueType = ExerciseSet.ValueType.COUNTDOWN),
        ExerciseSet(exerciseId = 0L, value = 3, valueType = ExerciseSet.ValueType.COUNTDOWN),
      )
    )
    val viewmodel = ExerciseScreenViewModel(
      fetchData = { data },
      saveProgress = { TestCase.fail() },
      timer = object : ITimer {
        override fun cleanup() {
          timerCleaned = true
        }
      })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    viewmodel.startSet()
    TestCase.assertFalse(timerCleaned)
    TestCase.assertEquals(
      true, viewmodel.uiState.value?.currentSet?.value?.timerState?.isActive?.value
    )

    viewmodel.cancelCountdown()
    TestCase.assertTrue(timerCleaned)
    TestCase.assertEquals(
      false, viewmodel.uiState.value?.currentSet?.value?.timerState?.isActive?.value
    )
  }
  @Test
  fun `background service cancelled`() = runTest(UnconfinedTestDispatcher()) {
    var bgCancelled = false
    val data = ExerciseWithSets(
      exercise = Exercise(routineId = 0L), sets = listOf(
        ExerciseSet(exerciseId = 0L, value = 3, valueType = ExerciseSet.ValueType.COUNTDOWN),
        ExerciseSet(exerciseId = 0L, value = 3, valueType = ExerciseSet.ValueType.COUNTDOWN),
      )
    )
    val viewmodel = ExerciseScreenViewModel(
      fetchData = { data },
      saveProgress = { TestCase.fail() },
      timer = object : ITimer {})

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    viewmodel.startSet(backgroundService = object : ICountdownTimerService {
      override fun cancel() {
        bgCancelled = true
      }
    })
    TestCase.assertFalse(bgCancelled)

    viewmodel.cancelCountdown()
    TestCase.assertTrue(bgCancelled)
  }

  @Test
  fun `set not changed`() = runTest(UnconfinedTestDispatcher()) {
    val data = ExerciseWithSets(
      exercise = Exercise(routineId = 0L), sets = listOf(
        ExerciseSet(exerciseId = 0L, value = 3, valueType = ExerciseSet.ValueType.COUNTDOWN),
        ExerciseSet(exerciseId = 0L, value = 3, valueType = ExerciseSet.ValueType.COUNTDOWN),
      )
    )
    val viewmodel = ExerciseScreenViewModel(
      fetchData = { data },
      saveProgress = { TestCase.fail() },
      timer = object : ITimer {})

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    viewmodel.startSet()
    TestCase.assertEquals(0, viewmodel.uiState.value?.currentSet?.value?.index)

    viewmodel.cancelCountdown()
    TestCase.assertEquals(0, viewmodel.uiState.value?.currentSet?.value?.index)
  }
}