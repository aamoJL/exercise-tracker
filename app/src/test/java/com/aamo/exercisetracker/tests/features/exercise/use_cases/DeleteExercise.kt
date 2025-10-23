package com.aamo.exercisetracker.tests.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.features.exercise.use_cases.deleteExercise
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

class DeleteExercise {
  @Test
  fun `returns true when deleted`() = runBlocking {
    val result = deleteExercise(
      exercise = Exercise(id = 1L, routineId = 1L), deleteData = { exercise -> true })

    assertTrue(result)
  }

  @Test
  fun `returns false when not deleted`() = runBlocking {
    val result = deleteExercise(
      exercise = Exercise(id = 0L, routineId = 1L), deleteData = { exercise -> false })

    assertFalse(result)
  }
}