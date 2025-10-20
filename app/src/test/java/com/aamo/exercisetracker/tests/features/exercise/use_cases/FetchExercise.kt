package com.aamo.exercisetracker.tests.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithProgressAndSets
import com.aamo.exercisetracker.features.exercise.ExerciseScreenViewModel
import com.aamo.exercisetracker.features.exercise.use_cases.fetchExercise
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
class FetchExercise {
  @Test
  fun `returns correct item when repetition`() = runBlocking {
    val set = ExerciseSet(
      exerciseId = 0, value = 5, unit = "Set Unit", valueType = ExerciseSet.ValueType.REPETITION
    )
    val exercise = ExerciseWithProgressAndSets(
      exercise = Exercise(id = 0, routineId = 1, name = "Exercise", restDuration = 2.minutes),
      sets = listOf(set),
      progress = ExerciseProgress(
        exerciseId = 0, finishedDate = Date()
      )
    )

    val result = fetchExercise(
      fetchData = { exercise })

    assertEquals(
      ExerciseScreenViewModel.Model(
        exerciseName = exercise.exercise.name,
        routineId = exercise.exercise.routineId,
        sets = listOf(
          ExerciseScreenViewModel.Model.SetModel(
            repetitions = set.value,
            setDuration = null,
            restDuration = exercise.exercise.restDuration,
            unit = set.unit
          )
        )
      ), result
    )
  }

  @Test
  fun `returns correct item when countdown`() = runBlocking {
    val set = ExerciseSet(
      exerciseId = 0,
      value = 5.minutes.inWholeMilliseconds.toInt(),
      unit = "Set Unit",
      valueType = ExerciseSet.ValueType.COUNTDOWN
    )
    val exercise = ExerciseWithProgressAndSets(
      exercise = Exercise(id = 0, routineId = 1, name = "Exercise", restDuration = 2.minutes),
      sets = listOf(set),
      progress = ExerciseProgress(
        exerciseId = 0, finishedDate = Date()
      )
    )

    val result = fetchExercise(
      fetchData = { exercise })

    assertEquals(
      ExerciseScreenViewModel.Model(
        exerciseName = exercise.exercise.name,
        routineId = exercise.exercise.routineId,
        sets = listOf(
          ExerciseScreenViewModel.Model.SetModel(
            repetitions = null,
            setDuration = set.value.milliseconds,
            restDuration = exercise.exercise.restDuration,
            unit = set.unit
          )
        )
      ), result
    )
  }
}