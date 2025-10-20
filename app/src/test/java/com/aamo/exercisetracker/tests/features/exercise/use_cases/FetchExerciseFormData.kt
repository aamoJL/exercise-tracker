package com.aamo.exercisetracker.tests.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.features.exercise.ExerciseFormViewModel
import com.aamo.exercisetracker.features.exercise.use_cases.fetchExerciseFormData
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
class FetchExerciseFormData {
  @Test
  fun `returns correct model when new`() = runBlocking {
    val id = 0L
    val result = fetchExerciseFormData(id = id, fetchData = { null })

    assertEquals(
      ExerciseFormViewModel.Model(
        exerciseName = String.EMPTY,
        restDuration = 0.minutes,
        setUnit = String.EMPTY,
        setAmounts = listOf(0),
        hasTimer = false,
        isNew = true
      ), result
    )
  }

  @Test
  fun `returns correct model when existing with repetition`() = runBlocking {
    val id = 1L
    val exercise = ExerciseWithSets(
      exercise = Exercise(routineId = 1L, name = "Exercise", restDuration = 2.minutes),
      sets = listOf(
        ExerciseSet(
          exerciseId = id,
          unit = "Set Unit",
          value = 30,
          valueType = ExerciseSet.ValueType.REPETITION
        )
      )
    )
    val result = fetchExerciseFormData(id = id, fetchData = { exercise })

    assertEquals(
      ExerciseFormViewModel.Model(
        exerciseName = exercise.exercise.name,
        restDuration = exercise.exercise.restDuration,
        setUnit = exercise.sets.first().unit,
        setAmounts = exercise.sets.map { it.value },
        hasTimer = false,
        isNew = false
      ), result
    )
  }

  @Test
  fun `returns correct model when existing with countdown`() = runBlocking {
    val id = 1L
    val exercise = ExerciseWithSets(
      exercise = Exercise(routineId = 1L, name = "Exercise", restDuration = 2.minutes),
      sets = listOf(
        ExerciseSet(
          exerciseId = id,
          unit = "Set Unit",
          value = 2.minutes.inWholeMilliseconds.toInt(),
          valueType = ExerciseSet.ValueType.COUNTDOWN
        )
      )
    )
    val result = fetchExerciseFormData(id = id, fetchData = { exercise })

    assertEquals(
      ExerciseFormViewModel.Model(
        exerciseName = exercise.exercise.name,
        restDuration = exercise.exercise.restDuration,
        setUnit = exercise.sets.first().unit,
        setAmounts = exercise.sets.map { it.value.milliseconds.inWholeMinutes.toInt() },
        hasTimer = true,
        isNew = false
      ), result
    )
  }

  @Test
  fun `throws when fetch error`() {
    assertThrows(Exception::class.java) {
      runBlocking {
        fetchExerciseFormData(id = 1L, fetchData = { null })
      }
    }
  }
}