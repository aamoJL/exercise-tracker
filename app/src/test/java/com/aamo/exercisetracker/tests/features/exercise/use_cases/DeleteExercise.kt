package com.aamo.exercisetracker.tests.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.features.exercise.use_cases.deleteExercise
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Test

class DeleteExercise {
  @Test
  fun `returns true when deleted`() = runBlocking {
    val result = deleteExercise(exerciseId = 1L, fetchData = { id ->
      Exercise(id = id, routineId = 1L)
    }, deleteData = { exercise -> true })

    assertTrue(result)
  }

  @Test
  fun `returns false when new`() = runBlocking {
    val result = deleteExercise(exerciseId = 0L, fetchData = { id ->
      Exercise(id = id, routineId = 1L)
    }, deleteData = { exercise -> true })

    assertFalse(result)
  }

  @Test
  fun `throws when fetch error`() = runBlocking {
    assertThrows(Exception::class.java) {
      runBlocking {
        deleteExercise(
          exerciseId = 1L,
          fetchData = { id -> null },
          deleteData = { exercise -> true })
      }
    }

    return@runBlocking
  }
}