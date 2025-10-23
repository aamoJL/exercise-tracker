package com.aamo.exercisetracker.tests.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.features.exercise.ExerciseFormViewModel
import com.aamo.exercisetracker.features.exercise.ExerciseScreenViewModel
import com.aamo.exercisetracker.features.exercise.use_cases.fromDao
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
class ExerciseScreenViewModelTests {
  @Test
  fun `returns correct model when repetition`() = runBlocking {
    val exercise = Exercise(id = 0, routineId = 1, name = "Exercise", restDuration = 2.minutes)
    val set = ExerciseSet(
      exerciseId = 0, value = 5, unit = "Set Unit", valueType = ExerciseSet.ValueType.REPETITION
    )

    val result = ExerciseScreenViewModel.Model.fromDao(
      fetchExercise = { exercise },
      fetchSets = { listOf(set) })

    assertEquals(
      ExerciseScreenViewModel.Model(
        exerciseName = exercise.name, routineId = exercise.routineId, sets = listOf(
          ExerciseScreenViewModel.Model.SetModel(
            repetitions = set.value,
            setDuration = null,
            restDuration = exercise.restDuration,
            unit = set.unit
          )
        )
      ), result
    )
  }

  @Test
  fun `returns correct model when countdown`() = runBlocking {
    val exercise = Exercise(id = 0, routineId = 1, name = "Exercise", restDuration = 2.minutes)
    val set = ExerciseSet(
      exerciseId = 0,
      value = 5.minutes.inWholeMilliseconds.toInt(),
      unit = "Set Unit",
      valueType = ExerciseSet.ValueType.COUNTDOWN
    )

    val result = ExerciseScreenViewModel.Model.fromDao(
      fetchExercise = { exercise },
      fetchSets = { listOf(set) })

    assertEquals(
      ExerciseScreenViewModel.Model(
        exerciseName = exercise.name, routineId = exercise.routineId, sets = listOf(
          ExerciseScreenViewModel.Model.SetModel(
            repetitions = null,
            setDuration = set.value.milliseconds,
            restDuration = exercise.restDuration,
            unit = set.unit
          )
        )
      ), result
    )
  }

  @Test
  fun `throws when fetch error`() {
    assertThrows(Exception::class.java) {
      runBlocking {
        ExerciseScreenViewModel.Model.fromDao(fetchExercise = { null }, fetchSets = { emptyList() })
      }
    }
  }
}

@Suppress("HardCodedStringLiteral")
class ExerciseFormViewModelTests {
  @Test
  fun `returns correct model when new`() {
    val exercise = Exercise(routineId = 0L)
    val set = ExerciseSet(exerciseId = exercise.id, value = 0)

    val result = runBlocking {
      ExerciseFormViewModel.Model.fromDao(fetchExercise = { exercise }, fetchSets = { listOf(set) })
    }

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
  fun `returns correct model when existing with repetition`() {
    val exercise = Exercise(id = 1L, routineId = 1, name = "Exercise", restDuration = 2.minutes)
    val set = ExerciseSet(
      exerciseId = exercise.id,
      value = 5,
      unit = "Set Unit",
      valueType = ExerciseSet.ValueType.REPETITION
    )

    val result = runBlocking {
      ExerciseFormViewModel.Model.fromDao(fetchExercise = { exercise }, fetchSets = { listOf(set) })
    }

    assertEquals(
      ExerciseFormViewModel.Model(
        exerciseName = exercise.name,
        restDuration = exercise.restDuration,
        setUnit = set.unit,
        setAmounts = listOf(set.value),
        hasTimer = false,
        isNew = false
      ), result
    )
  }

  @Test
  fun `returns correct model when existing with countdown`() {
    val exercise = Exercise(id = 1L, routineId = 1, name = "Exercise", restDuration = 2.minutes)
    val set = ExerciseSet(
      exerciseId = exercise.id,
      value = 5.minutes.inWholeMilliseconds.toInt(),
      unit = "Set Unit",
      valueType = ExerciseSet.ValueType.COUNTDOWN
    )

    val result = runBlocking {
      ExerciseFormViewModel.Model.fromDao(fetchExercise = { exercise }, fetchSets = { listOf(set) })
    }

    assertEquals(
      ExerciseFormViewModel.Model(
        exerciseName = exercise.name,
        restDuration = exercise.restDuration,
        setUnit = set.unit,
        setAmounts = listOf(set.value.milliseconds.inWholeMinutes.toInt()),
        hasTimer = true,
        isNew = false
      ), result
    )
  }

  @Test
  fun `throws when fetch error`() {
    assertThrows(Exception::class.java) {
      runBlocking {
        ExerciseFormViewModel.Model.fromDao(fetchExercise = { null }, fetchSets = { emptyList() })
      }
    }
  }
}