package com.aamo.exercisetracker.tests.database.dao.routine_dao

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithProgress
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithExerciseProgresses
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Calendar

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Get : DatabaseTest() {
  @Test
  fun getScheduleByRoutineId() = runTest {
    routineDao.upsert(Routine()).also { routineId ->
      val schedule = RoutineSchedule(routineId = routineId, monday = true)

      routineDao.upsert(schedule).also { scheduleId ->
        val expected = schedule.copy(id = scheduleId)
        val actual = routineDao.getScheduleByRoutineId(routineId)

        assertEquals(expected, actual)
      }
    }
  }

  @Test
  fun getExerciseSets() = runTest {
    routineDao.upsert(Routine()).also { routineId ->
      val exercise = Exercise(routineId = routineId, name = "Name")

      routineDao.upsert(exercise).also { exerciseId ->
        val sets = listOf(
          ExerciseSet(exerciseId = exerciseId, value = 1),
          ExerciseSet(exerciseId = exerciseId, value = 2),
          ExerciseSet(exerciseId = exerciseId, value = 3),
        )

        routineDao.upsert(*sets.toTypedArray())

        val expected = sets.mapIndexed { i, set -> set.copy(id = i + 1L) }
        val actual = routineDao.getExerciseSets(exerciseId)

        assertEquals(expected, actual)
      }
    }
  }

  @Test
  fun getExerciseWithSets() = runTest {
    routineDao.upsert(Routine()).also { routineId ->
      val exercise = Exercise(routineId = routineId, name = "Name")

      routineDao.upsert(exercise).also { exerciseId ->
        val sets = listOf(
          ExerciseSet(exerciseId = exerciseId, value = 1),
          ExerciseSet(exerciseId = exerciseId, value = 2),
          ExerciseSet(exerciseId = exerciseId, value = 3),
        )

        routineDao.upsert(*sets.toTypedArray())

        val expected = ExerciseWithSets(
          exercise = exercise.copy(id = exerciseId),
          sets = sets.mapIndexed { i, set -> set.copy(id = i + 1L) })
        val actual = routineDao.getExerciseWithSets(exerciseId)

        assertEquals(expected, actual)
      }
    }
  }

  @Test
  fun getExerciseProgressByExerciseId() = runTest {
    routineDao.upsert(Routine()).also { routineId ->
      val exercise = Exercise(routineId = routineId, name = "Name")

      routineDao.upsert(exercise).also { exerciseId ->
        val progress = ExerciseProgress(exerciseId = exerciseId)

        routineDao.upsert(progress).also { progressId ->
          val expected = progress.copy(id = progressId)
          val actual = routineDao.getExerciseProgressByExerciseId(exerciseId)

          assertEquals(expected, actual)
        }
      }
    }
  }

  @Test
  fun getRoutineWithSchedule() = runTest {
    val routine = Routine()

    routineDao.upsert(routine).also { routineId ->
      val schedule = RoutineSchedule(routineId = routineId)

      routineDao.upsert(schedule).also { scheduleId ->
        val expected = RoutineWithSchedule(
          routine = routine.copy(id = routineId), schedule = schedule.copy(id = scheduleId)
        )
        val actual = routineDao.getRoutineWithSchedule(routineId)

        assertEquals(expected, actual)
      }
    }
  }

  @Test
  fun getRoutinesWithScheduleFlow() = runTest {
    val routine = Routine()

    routineDao.upsert(routine).also { routineId ->
      val schedule = RoutineSchedule(routineId = routineId)

      routineDao.upsert(schedule).also { scheduleId ->
        val expected = listOf(
          RoutineWithSchedule(
            routine = routine.copy(id = routineId), schedule = schedule.copy(id = scheduleId)
          )
        )
        val actual = routineDao.getRoutinesWithScheduleFlow().first()

        assertEquals(expected, actual)
      }
    }
  }

  @Test
  fun getRoutineWithProgressesFlow() = runTest {
    val routine = Routine()

    routineDao.upsert(routine).also { routineId ->
      val exercise = Exercise(routineId = routineId)

      routineDao.upsert(exercise).also { exerciseId ->
        val progress = ExerciseProgress(
          exerciseId = exerciseId, finishedDate = Calendar.getInstance().time
        )

        routineDao.upsert(progress).also { progressId ->
          val actual = routineDao.getRoutineWithProgressesFlow(routineId).first()
          val expected = RoutineWithExerciseProgresses(
            routine = routine.copy(id = routineId), exerciseProgresses = listOf(
              ExerciseWithProgress(
                exercise = exercise.copy(id = exerciseId), progress = progress.copy(id = progressId)
              )
            )
          )

          assertEquals(expected, actual)
        }
      }
    }
  }

  @Test
  fun getRoutineSchedulesWithProgressesFlow() = runTest {
    val routine = Routine()

    routineDao.upsert(routine).also { routineId ->
      val exercise = Exercise(routineId = routineId)

      routineDao.upsert(exercise).also { exerciseId ->
        val schedule = RoutineSchedule(routineId = routineId)

        routineDao.upsert(schedule).also { scheduleId ->
          val progress = ExerciseProgress(
            exerciseId = exerciseId, finishedDate = Calendar.getInstance().time
          )

          routineDao.upsert(progress).also { progressId ->
            val expected = mapOf(
              RoutineWithSchedule(
                routine = routine.copy(id = routineId), schedule = schedule.copy(id = scheduleId)
              ) to listOf(
                ExerciseWithProgress(
                  exercise = exercise.copy(id = exerciseId),
                  progress = progress.copy(id = progressId)
                )
              )
            )
            val actual = routineDao.getRoutineSchedulesWithProgressesFlow().first()

            assertEquals(expected, actual)
          }
        }
      }
    }
  }

  @Test
  fun `getRoutineSchedulesWithProgressesFlow should not return routines without exercises`() =
    runTest {
      val routine = Routine(name = "Name").let { routine ->
        routineDao.upsert(routine).let { routine.copy(id = it) }
      }
      RoutineSchedule(routineId = routine.id).let { schedule ->
        routineDao.upsert(schedule).let { schedule.copy(id = it) }
      }

      val result = routineDao.getRoutineSchedulesWithProgressesFlow().first().entries.firstOrNull()

      Assert.assertEquals(null, result)
    }

  @Test
  fun `getRoutineSchedulesWithProgressesFlow should not return routines without schedules`() =
    runTest {
      val routine = Routine(name = "Name").let { routine ->
        routineDao.upsert(routine).let { routine.copy(id = it) }
      }
      Exercise(routineId = routine.id).let { exercise ->
        routineDao.upsert(exercise).let { exercise.copy(id = it) }
      }

      val result = routineDao.getRoutineSchedulesWithProgressesFlow().first().entries.firstOrNull()

      Assert.assertEquals(null, result)
    }
}