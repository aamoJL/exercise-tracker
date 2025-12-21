package com.aamo.exercisetracker.tests.features.exercise.view.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.features.exercise.view.use_cases.saveExerciseProgress
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class SaveExerciseProgress : DatabaseTest() {
  @Test
  fun `saves correct model when new`() = runTest {
    val progress = ExerciseProgress(
      id = 0L,
      exerciseId = routineDao.upsert(exercise = Exercise(routineId = routineDao.upsert(Routine()))),
      finishedDate = Date(1),
    )

    saveExerciseProgress(dao = routineDao, progress = progress)

    val result = routineDao.getExerciseProgressByExerciseId(exerciseId = progress.exerciseId)

    checkNotNull(result)
    assertEquals(progress.copy(id = result.id), result)
  }

  @Test
  fun `updates correct model when existing`() = runTest {
    val progress = ExerciseProgress(
      exerciseId = routineDao.upsert(exercise = Exercise(routineId = routineDao.upsert(Routine()))),
      finishedDate = Date(1),
    ).let {
      routineDao.upsert(it).let { id -> it.copy(id = id) }
    }

    val newDate = Date(999)
    saveExerciseProgress(dao = routineDao, progress = progress.copy(finishedDate = newDate))

    val result = routineDao.getExerciseProgressByExerciseId(exerciseId = progress.exerciseId)
    checkNotNull(result)
    assertEquals(progress.copy(finishedDate = newDate), result)
  }
}