package com.aamo.exercisetracker.tests.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.features.exercise.use_cases.saveExerciseProgress
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.Date

class SaveExerciseProgress {
  @Test
  fun `saves correctly when new`() {
    val progress = ExerciseProgress(exerciseId = 0L, finishedDate = Date(1))
    val date = Date(10)

    var result: ExerciseProgress? = null

    assert(runBlocking {
      saveExerciseProgress(
        finishedDate = date,
        progress = progress,
        saveData = { value -> true.also { result = value } })
    })

    assertNotNull(result)
    assertNotEquals(progress, result)
    assertEquals(date, result?.finishedDate)
  }

  @Test
  fun `updates correctly when existing`() = runBlocking {
    val oldProgress = ExerciseProgress(exerciseId = 0, finishedDate = Date(0))
    var updatedProgress: ExerciseProgress? = null
    val newDate = Date(10)

    saveExerciseProgress(
      finishedDate = newDate,
      progress = oldProgress,
      saveData = { value -> true.also { updatedProgress = value } })

    assertNotNull(updatedProgress)
    assertNotEquals(oldProgress, updatedProgress)
    assertEquals(newDate, updatedProgress?.finishedDate)
  }
}