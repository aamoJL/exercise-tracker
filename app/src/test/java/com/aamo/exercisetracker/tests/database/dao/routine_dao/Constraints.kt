package com.aamo.exercisetracker.tests.database.dao.routine_dao

import android.database.sqlite.SQLiteConstraintException
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.test_utility.database.RoutineDatabaseTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
class Constraints : RoutineDatabaseTest() {
  @Test(expected = SQLiteConstraintException::class)
  fun `schedule upsert throws exception if routine does not exist`() = runTest {
    dao.upsert(RoutineSchedule(routineId = 1L))
  }

  @Test(expected = SQLiteConstraintException::class)
  fun `exercise progress upsert throws exception if exercise does not exist`() = runTest {
    dao.upsert(ExerciseProgress(exerciseId = 1L, finishedDate = Calendar.getInstance().time))
  }

  @Test(expected = SQLiteConstraintException::class)
  fun `exercise upsert throws exception if routine does not exist`() = runTest {
    dao.upsert(Exercise(routineId = 1L))
  }

  @Test(expected = SQLiteConstraintException::class)
  fun `exercise set upsert throws exception if exercise does not exist`() = runTest {
    dao.upsert(ExerciseSet(exerciseId = 1L))
  }
}