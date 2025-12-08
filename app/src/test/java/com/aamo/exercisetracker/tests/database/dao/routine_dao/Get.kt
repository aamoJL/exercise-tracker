package com.aamo.exercisetracker.tests.database.dao.routine_dao

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithProgress
import com.aamo.exercisetracker.database.entities.ExerciseWithProgressAndSets
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithExerciseProgresses
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.test_utility.database.RoutineDatabaseTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Calendar

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Get : RoutineDatabaseTest() {
  @Test
  fun getRoutine() = runTest {
    val routine = Routine()

    dao.upsert(routine).also { id ->
      val expected = routine.copy(id = id)
      val actual = dao.getRoutine(id)

      assertEquals(expected, actual)
    }

    assertNull(dao.getRoutine(0))
  }

  @Test
  fun getScheduleByRoutineId() = runTest {
    dao.upsert(Routine()).also { routineId ->
      val schedule = RoutineSchedule(routineId = routineId, monday = true)

      dao.upsert(schedule).also { scheduleId ->
        val expected = schedule.copy(id = scheduleId)
        val actual = dao.getScheduleByRoutineId(routineId)

        assertEquals(expected, actual)
      }
    }
  }

  @Test
  fun getExercise() = runTest {
    dao.upsert(Routine()).also { routineId ->
      val exercise = Exercise(routineId = routineId, name = "Name")

      dao.upsert(exercise).also { exerciseId ->
        val expected = exercise.copy(id = exerciseId)
        val actual = dao.getExercise(exerciseId)

        assertEquals(expected, actual)
      }
    }
  }

  @Test
  fun getExerciseSets() = runTest {
    dao.upsert(Routine()).also { routineId ->
      val exercise = Exercise(routineId = routineId, name = "Name")

      dao.upsert(exercise).also { exerciseId ->
        val sets = listOf(
          ExerciseSet(exerciseId = exerciseId, value = 1),
          ExerciseSet(exerciseId = exerciseId, value = 2),
          ExerciseSet(exerciseId = exerciseId, value = 3),
        )

        dao.upsert(*sets.toTypedArray())

        val expected = sets.mapIndexed { i, set -> set.copy(id = i + 1L) }
        val actual = dao.getExerciseSets(exerciseId)

        assertEquals(expected, actual)
      }
    }
  }

  @Test
  fun getExerciseWithSets() = runTest {
    dao.upsert(Routine()).also { routineId ->
      val exercise = Exercise(routineId = routineId, name = "Name")

      dao.upsert(exercise).also { exerciseId ->
        val sets = listOf(
          ExerciseSet(exerciseId = exerciseId, value = 1),
          ExerciseSet(exerciseId = exerciseId, value = 2),
          ExerciseSet(exerciseId = exerciseId, value = 3),
        )

        dao.upsert(*sets.toTypedArray())

        val expected = ExerciseWithSets(
          exercise = exercise.copy(id = exerciseId),
          sets = sets.mapIndexed { i, set -> set.copy(id = i + 1L) })
        val actual = dao.getExerciseWithSets(exerciseId)

        assertEquals(expected, actual)
      }
    }
  }

  @Test
  fun getExerciseProgressByExerciseId() = runTest {
    dao.upsert(Routine()).also { routineId ->
      val exercise = Exercise(routineId = routineId, name = "Name")

      dao.upsert(exercise).also { exerciseId ->
        val progress = ExerciseProgress(exerciseId = exerciseId)

        dao.upsert(progress).also { progressId ->
          val expected = progress.copy(id = progressId)
          val actual = dao.getExerciseProgressByExerciseId(exerciseId)

          assertEquals(expected, actual)
        }
      }
    }
  }

  @Test
  fun getRoutineWithSchedule() = runTest {
    val routine = Routine()

    dao.upsert(routine).also { routineId ->
      val schedule = RoutineSchedule(routineId = routineId)

      dao.upsert(schedule).also { scheduleId ->
        val expected = RoutineWithSchedule(
          routine = routine.copy(id = routineId), schedule = schedule.copy(id = scheduleId)
        )
        val actual = dao.getRoutineWithSchedule(routineId)

        assertEquals(expected, actual)
      }
    }
  }

  @Test
  fun getRoutinesWithScheduleFlow() = runTest {
    val routine = Routine()

    dao.upsert(routine).also { routineId ->
      val schedule = RoutineSchedule(routineId = routineId)

      dao.upsert(schedule).also { scheduleId ->
        val expected = listOf(
          RoutineWithSchedule(
            routine = routine.copy(id = routineId), schedule = schedule.copy(id = scheduleId)
          )
        )
        val actual = dao.getRoutinesWithScheduleFlow().first()

        assertEquals(expected, actual)
      }
    }
  }

  @Test
  fun getRoutineWithProgressesFlow() = runTest {
    val routine = Routine()

    dao.upsert(routine).also { routineId ->
      val exercise = Exercise(routineId = routineId)

      dao.upsert(exercise).also { exerciseId ->
        val progress = ExerciseProgress(
          exerciseId = exerciseId, finishedDate = Calendar.getInstance().time
        )

        dao.upsert(progress).also { progressId ->
          val actual = dao.getRoutineWithProgressesFlow(routineId).first()
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
  fun getExerciseWithProgressAndSets() = runTest {
    dao.upsert(Routine()).also { routineId ->
      val exercise = Exercise(routineId = routineId)

      dao.upsert(exercise).also { exerciseId ->
        val progress = ExerciseProgress(
          exerciseId = exerciseId, finishedDate = Calendar.getInstance().time
        )

        dao.upsert(progress).also { progressId ->
          val set = ExerciseSet(exerciseId = exerciseId)

          dao.upsert(set).also {
            val expected = ExerciseWithProgressAndSets(
              exercise = exercise.copy(id = exerciseId),
              sets = listOf(set.copy(id = 1L)),
              progress = progress.copy(id = progressId)
            )
            val actual = dao.getExerciseWithProgressAndSets(exerciseId = exerciseId)

            assertEquals(expected, actual)
          }
        }
      }
    }
  }

  @Test
  fun getRoutineSchedulesWithProgressesFlow() = runTest {
    val routine = Routine()

    dao.upsert(routine).also { routineId ->
      val exercise = Exercise(routineId = routineId)

      dao.upsert(exercise).also { exerciseId ->
        val schedule = RoutineSchedule(routineId = routineId)

        dao.upsert(schedule).also { scheduleId ->
          val progress = ExerciseProgress(
            exerciseId = exerciseId, finishedDate = Calendar.getInstance().time
          )

          dao.upsert(progress).also { progressId ->
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
            val actual = dao.getRoutineSchedulesWithProgressesFlow().first()

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
        dao.upsert(routine).let { routine.copy(id = it) }
      }
      RoutineSchedule(routineId = routine.id).let { schedule ->
        dao.upsert(schedule).let { schedule.copy(id = it) }
      }

      val result = dao.getRoutineSchedulesWithProgressesFlow().first().entries.firstOrNull()

      Assert.assertEquals(null, result)
    }

  @Test
  fun `getRoutineSchedulesWithProgressesFlow should not return routines without schedules`() =
    runTest {
      val routine = Routine(name = "Name").let { routine ->
        dao.upsert(routine).let { routine.copy(id = it) }
      }
      Exercise(routineId = routine.id).let { exercise ->
        dao.upsert(exercise).let { exercise.copy(id = it) }
      }

      val result = dao.getRoutineSchedulesWithProgressesFlow().first().entries.firstOrNull()

      Assert.assertEquals(null, result)
    }
}