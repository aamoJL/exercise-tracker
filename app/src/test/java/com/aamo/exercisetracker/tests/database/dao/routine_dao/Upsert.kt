package com.aamo.exercisetracker.tests.database.dao.routine_dao

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Upsert : DatabaseTest() {
  @Test
  fun `upsert exercise with sets`() = runTest {
    routineDao.upsert(Routine()).also { routineId ->
      val exercise = Exercise(routineId = routineId)
      val set = ExerciseSet(exerciseId = 0L)

      routineDao.upsert(exercise = exercise, sets = listOf(set)).also { exerciseId ->
        routineDao.getExerciseWithSets(exerciseId = exerciseId).also { result ->
          checkNotNull(result)
          assertEquals(exercise.copy(id = exerciseId), result.exercise)
          assertEquals(1, result.sets.size)
          result.sets.first().also { assertEquals(exerciseId, it.exerciseId) }
        }
      }
    }
  }

  @Test
  fun `upsert routine with schedule`() = runTest {
    val routine = Routine()
    val schedule = RoutineSchedule(routineId = 0L)

    routineDao.upsert(routine = routine, schedule = schedule).also { (routineId, scheduleId) ->
      checkNotNull(scheduleId)
      routineDao.getRoutineWithSchedule(routineId = routineId).also { result ->
        checkNotNull(result)
        assertEquals(
          RoutineWithSchedule(
            routine = routine.copy(id = routineId),
            schedule = schedule.copy(id = scheduleId, routineId = routineId)
          ), result
        )
      }
    }
  }
}