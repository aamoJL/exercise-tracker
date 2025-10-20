package com.aamo.exercisetracker.tests.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.features.exercise.use_cases.updateExerciseProgress
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.Date

class UpdateExerciseProgress {
  @Test
  fun `updates correctly when null`() = runBlocking {
    val newId = 2L
    var result = false

    updateExerciseProgress(
      exerciseId = newId,
      finishedDate = Date(),
      fetchData = { null },
      saveData = { value -> true.also { result = value.exerciseId == newId } })

    assert(result)
  }

  @Test
  fun `updates correctly when existing`() = runBlocking {
    val oldProgress = ExerciseProgress(exerciseId = 0, finishedDate = Date(0))
    var newProgress: ExerciseProgress? = null
    val newDate = Date(10)

    updateExerciseProgress(
      exerciseId = 0,
      finishedDate = newDate,
      fetchData = { oldProgress },
      saveData = { value -> true.also { newProgress = value } })

    assertNotNull(newProgress)
    assertNotEquals(oldProgress, newProgress)
    assertEquals(newDate, newProgress?.finishedDate)
  }
}