@file:Suppress("HardCodedStringLiteral")

package com.aamo.exercisetracker.database

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithProgress
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineDao
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithScheduleAndExerciseProgresses
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class RoutineDaoTests {
  private lateinit var database: RoutineDatabase
  private lateinit var dao: RoutineDao

  // region Setup
  @Before
  fun setupDatabase() {
    database = Room.inMemoryDatabaseBuilder(
      context = ApplicationProvider.getApplicationContext(), klass = RoutineDatabase::class.java
    ).build()
    dao = database.routineDao()
  }

  @After
  @Throws(IOException::class)
  fun closeDatabase() {
    database.close()
  }
  // endregion

  // region INSERT & GET
  @Test
  fun `get routine`() = runTest {
    val routine = Routine(id = 0, name = "New routine")

    dao.upsert(routine).also { insertRoutineId ->
      dao.getRoutine(insertRoutineId).also { result ->
        checkNotNull(result)
        assertEquals(routine.copy(id = insertRoutineId), result)
      }
    }
  }

  @Test
  fun `get schedule by routine id`() = runTest {
    val routine = Routine(id = 0, name = "New routine")

    dao.upsert(routine).also { insertRoutineId ->
      val schedule = RoutineSchedule(id = 0, routineId = insertRoutineId)

      dao.upsert(schedule).also { insertScheduleId ->
        dao.getScheduleByRoutineId(insertRoutineId).also { result ->
          checkNotNull(result)
          assertEquals(schedule.copy(id = insertScheduleId, routineId = insertRoutineId), result)
        }
      }
    }
  }

  @Test
  fun `get exercise`() = runTest {
    dao.upsert(routine = Routine(name = "Name")).also { insertRoutineId ->
      val exercise = Exercise(routineId = insertRoutineId)

      dao.upsert(exercise).also { insertExerciseId ->
        dao.getExercise(exerciseId = insertExerciseId).also { result ->
          checkNotNull(result)
          assertEquals(exercise.copy(id = insertExerciseId), result)
        }
      }
    }
  }

  @Test
  fun `get exercise progress by exercise id`() = runTest {
    dao.upsert(routine = Routine(name = "Name")).also { insertRoutineId ->
      val exercise = Exercise(routineId = insertRoutineId)

      dao.upsert(exercise).also { insertExerciseId ->
        val progress = ExerciseProgress(
          exerciseId = insertExerciseId, finishedDate = Calendar.getInstance().time
        )

        dao.upsert(progress).also { insertProgressId ->
          dao.getExerciseProgressByExerciseId(exerciseId = insertExerciseId).also { result ->
            checkNotNull(result)
            assertEquals(progress.copy(id = insertProgressId), result)
          }
        }
      }
    }
  }

  @Test
  fun `get routine with schedule`() = runTest {
    val routine = Routine(id = 0, name = "New routine")

    dao.upsert(routine = routine).also { insertRoutineId ->
      val schedule = RoutineSchedule(id = 0, routineId = insertRoutineId)

      dao.upsert(schedule).also { insertScheduleId ->
        dao.getRoutineWithSchedule(routineId = insertRoutineId).also { result ->
          checkNotNull(result)
          assertEquals(
            RoutineWithSchedule(
              routine = routine.copy(id = insertRoutineId),
              schedule = schedule.copy(id = insertScheduleId)
            ), result
          )
        }
      }
    }
  }

  @Test
  fun `get routines with schedule flow`() = runTest {
    val routine = Routine(id = 0, name = "New routine")

    dao.upsert(routine = routine).also { insertRoutineId ->
      val schedule = RoutineSchedule(id = 0, routineId = insertRoutineId)

      dao.upsert(schedule).also { insertScheduleId ->
        dao.getRoutinesWithScheduleFlow().first().also { result ->
          assertEquals(1, result.size)
          assertEquals(
            RoutineWithSchedule(
              routine = routine.copy(id = insertRoutineId),
              schedule = schedule.copy(id = insertScheduleId)
            ), result.first()
          )
        }
      }
    }
  }

  @Test
  fun `get routines with schedule and progresses flow`() = runTest {
    val routine = Routine(id = 0, name = "New routine")

    dao.upsert(routine = routine).also { insertRoutineId ->
      val schedule = RoutineSchedule(id = 0, routineId = insertRoutineId)

      dao.upsert(schedule).also { insertScheduleId ->
        val exercise = Exercise(routineId = insertRoutineId)

        dao.upsert(exercise).also { insertExerciseId ->
          val progress = ExerciseProgress(
            exerciseId = insertExerciseId, finishedDate = Calendar.getInstance().time
          )

          dao.upsert(progress).also { insertProgressId ->
            dao.getRoutinesWithScheduleAndProgressesFlow().first().also { result ->
              assertEquals(1, result.size)
              assertEquals(
                RoutineWithScheduleAndExerciseProgresses(
                  routine = routine.copy(id = insertRoutineId),
                  schedule = schedule.copy(id = insertScheduleId),
                  exerciseProgresses = listOf(
                    ExerciseWithProgress(
                      exercise = exercise.copy(id = insertExerciseId),
                      progress = progress.copy(id = insertProgressId),
                    )
                  ),
                ), result.first()
              )
            }
          }
        }
      }
    }
  }

  @Test
  fun `get exercise with sets`() = runTest {
    dao.upsert(routine = Routine(name = "Name")).also { insertRoutineId ->
      val exercise = Exercise(routineId = insertRoutineId)

      dao.upsert(exercise).also { insertExerciseId ->
        val set = ExerciseSet(exerciseId = insertExerciseId)

        dao.upsert(set).also {
          dao.getExerciseWithSets(exerciseId = insertExerciseId).also { result ->
            checkNotNull(result)
            assertEquals(exercise.copy(id = insertExerciseId), result.exercise)
            assertEquals(1, result.sets.size)
            result.sets.forEach { assertEquals(insertExerciseId, it.exerciseId) }
          }
        }
      }
    }
  }

  @Test
  fun `get routine with progresses flow`() = runTest {
    val routine = Routine(id = 0, name = "New routine")

    dao.upsert(routine = routine).also { insertRoutineId ->
      val exercise = Exercise(routineId = insertRoutineId)

      dao.upsert(exercise).also { insertExerciseId ->
        val progress = ExerciseProgress(
          exerciseId = insertExerciseId, finishedDate = Calendar.getInstance().time
        )

        dao.upsert(progress).also { insertProgressId ->
          dao.getRoutineWithProgressesFlow(routineId = insertRoutineId).first().also { result ->
            checkNotNull(result)
            assertEquals(routine.copy(id = insertRoutineId), result.routine)
            assertEquals(1, result.exerciseProgresses.size)
            result.exerciseProgresses.first().also { (e, p) ->
              checkNotNull(p)
              assertEquals(exercise.copy(id = insertExerciseId), e)
              assertEquals(progress.copy(id = insertProgressId), p)
            }
          }
        }
      }
    }
  }

  @Test
  fun `get exercise with progresses and sets`() = runTest {
    dao.upsert(routine = Routine(id = 0, name = "New routine")).also { insertRoutineId ->
      val exercise = Exercise(routineId = insertRoutineId)

      dao.upsert(exercise).also { insertExerciseId ->
        val progress = ExerciseProgress(
          exerciseId = insertExerciseId, finishedDate = Calendar.getInstance().time
        )

        dao.upsert(progress).also { insertProgressId ->
          val set = ExerciseSet(exerciseId = insertExerciseId)

          dao.upsert(set).also {
            dao.getExerciseWithProgressAndSets(exerciseId = insertExerciseId).also { result ->
              checkNotNull(result)
              assertEquals(exercise.copy(id = insertExerciseId), result.exercise)
              assertEquals(progress.copy(id = insertProgressId), result.progress)
              assertEquals(1, result.sets.size)
              result.sets.forEach { assertEquals(insertExerciseId, it.exerciseId) }
            }
          }
        }
      }
    }
  }

  @Test
  fun `upsert exercise with sets`() = runTest {
    dao.upsert(routine = Routine(id = 0, name = "New routine")).also { insertRoutineId ->
      val exercise = Exercise(routineId = insertRoutineId)
      val set = ExerciseSet(exerciseId = 0L)

      dao.upsert(exerciseWithSets = ExerciseWithSets(exercise = exercise, sets = listOf(set)))
        .also { insertExerciseId ->
          dao.getExerciseWithSets(exerciseId = insertExerciseId).also { result ->
            checkNotNull(result)
            assertEquals(exercise.copy(id = insertExerciseId), result.exercise)
            assertEquals(1, result.sets.size)
            result.sets.forEach { assertEquals(insertExerciseId, it.exerciseId) }
          }
        }
    }
  }

  @Test
  fun `upsert routine with schedule`() = runTest {
    val routine = Routine(id = 0, name = "New routine")
    val schedule = RoutineSchedule(id = 0, routineId = 0L)

    dao.upsert(routineWithSchedule = RoutineWithSchedule(routine = routine, schedule = schedule))
      .also { (insertRoutineId, insertScheduleId) ->
        checkNotNull(insertScheduleId)
        dao.getRoutineWithSchedule(routineId = insertRoutineId).also { result ->
          checkNotNull(result)
          assertEquals(
            RoutineWithSchedule(
              routine = routine.copy(id = insertRoutineId),
              schedule = schedule.copy(id = insertScheduleId, routineId = insertRoutineId)
            ), result
          )
        }
      }
  }

  @Test
  fun `upsert and get routine with schedule`() = runTest {
    val routine = Routine(id = 0, name = "New routine")
    val schedule = RoutineSchedule(id = 0, routineId = 0L)

    dao.upsertAndGet(
      routineWithSchedule = RoutineWithSchedule(routine = routine, schedule = schedule)
    ).also { result ->
      checkNotNull(result)
      checkNotNull(result.schedule)
      assertEquals(
        RoutineWithSchedule(
          routine = routine.copy(id = 1L), schedule = schedule.copy(id = 1L, routineId = 1L)
        ), result
      )
    }
  }
  // endregion

  // region UPSERT Constraint Tests
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
  // endregion
}