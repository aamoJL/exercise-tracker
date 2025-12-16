package com.aamo.exercisetracker.tests.features.exercise.form.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.features.exercise.form.use_cases.fetchExercise
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class FetchExercise : DatabaseTest() {
  @Test
  fun `returns correct model when new`() = runTest {
    val routineId = routineDao.upsert(Routine())
    val defaultUnit = "Default"

    val expected = ExerciseWithSets(
      exercise = Exercise(
        id = 0L, routineId = routineId, name = String.EMPTY, restDuration = 0.minutes
      ), sets = listOf(
        ExerciseSet(
          id = 0L,
          exerciseId = 0L,
          value = 0,
          unit = defaultUnit,
          valueType = ExerciseSet.ValueType.REPETITION
        )
      )
    )
    val actual = fetchExercise(
      dao = routineDao, exerciseId = 0L, routineId = routineId, defaultUnit = defaultUnit
    )

    assertEquals(expected, actual)
  }

  @Test
  fun `returns correct model when existing`() = runTest {
    val routineId = routineDao.upsert(Routine())

    val exercise =
      Exercise(routineId = routineId, name = "Exercise 1", restDuration = 3.minutes).let {
        routineDao.upsert(it).let { id -> it.copy(id = id) }
      }
    val set = ExerciseSet(
      exerciseId = exercise.id,
      value = 123,
      unit = "Unit",
      valueType = ExerciseSet.ValueType.REPETITION
    ).let {
      routineDao.upsert(it).let { id -> it.copy(id = 1L) }
    }

    val expected = ExerciseWithSets(exercise = exercise, sets = listOf(set))
    val actual = fetchExercise(
      dao = routineDao, exerciseId = exercise.id, routineId = routineId, defaultUnit = String.EMPTY
    )

    assertEquals(expected, actual)
  }
}