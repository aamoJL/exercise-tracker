package com.aamo.exercisetracker.tests.features.exercise.view.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.features.exercise.view.use_cases.fetchExercise
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class FetchExercise : DatabaseTest() {
  @Test
  fun `returns correct model`() = runTest {
    val exercise = Exercise(
      routineId = routineDao.upsert(Routine()), name = "Exercise 1", restDuration = 3.minutes
    ).let {
      routineDao.upsert(it).let { id -> it.copy(id = id) }
    }
    val sets = listOf(ExerciseSet(exerciseId = exercise.id, value = 2).let {
      routineDao.upsert(it).let { _ -> it.copy(id = 1L) }
    }, ExerciseSet(exerciseId = exercise.id, value = 3).let {
      routineDao.upsert(it).let { _ -> it.copy(id = 2L) }
    }, ExerciseSet(exerciseId = exercise.id, value = 1).let {
      routineDao.upsert(it).let { _ -> it.copy(id = 3L) }
    })

    val actual = fetchExercise(dao = routineDao, exerciseId = exercise.id)

    checkNotNull(actual)
    TestCase.assertEquals(exercise, actual.exercise)
    TestCase.assertEquals(sets, actual.sets)
  }
}