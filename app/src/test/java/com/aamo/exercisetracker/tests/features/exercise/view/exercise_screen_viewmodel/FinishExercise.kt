package com.aamo.exercisetracker.tests.features.exercise.view.exercise_screen_viewmodel

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.features.exercise.view.ExerciseScreenViewModel
import com.aamo.exercisetracker.utility.viewmodels.ITimer
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class FinishExercise {
  @Test
  fun `saveProgress called`() = runTest(UnconfinedTestDispatcher()) {
    var called: ExerciseProgress? = null
    val data = ExerciseWithSets(
      exercise = Exercise(routineId = 0L), sets = listOf(
        ExerciseSet(exerciseId = 0L, value = 123, valueType = ExerciseSet.ValueType.REPETITION)
      )
    )
    val viewmodel = ExerciseScreenViewModel(
      fetchData = { data },
      saveProgress = { called = it },
      timer = object : ITimer {})

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.uiState.collect()
    }

    val date = Date()
    viewmodel.finishExercise(date)

    TestCase.assertEquals(
      ExerciseProgress(id = 0L, exerciseId = data.exercise.id, finishedDate = date), called
    )
  }
}