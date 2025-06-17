package com.aamo.exercisetracker.database

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineDao
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.time.Duration.Companion.minutes

@RunWith(AndroidJUnit4::class)
class RoutineDaoTests {
  private lateinit var database: RoutineDatabase
  private lateinit var dao: RoutineDao

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

  @Test
  @Throws(Exception::class)
  fun `get routine`() = runTest {
    val newRoutine = Routine(id = 0, name = "New routine", 1.minutes)

    dao.upsert(newRoutine).also { insertId ->
      dao.getRoutine(insertId).also { insertedRoutine ->
        checkNotNull(insertedRoutine)
        assertEquals(newRoutine.copy(id = insertId), insertedRoutine)
      }
    }
  }

  @Test
  @Throws(Exception::class)
  fun `insert and update routine using upsert`() = runTest {
    val newRoutine = Routine(id = 0, name = "New routine", 1.minutes)

    dao.upsert(newRoutine).also { insertId ->
      assert(insertId != -1L)
      assert(insertId != 0L)

      dao.getRoutine(insertId).also { insertedRoutine ->
        checkNotNull(insertedRoutine)
        assertEquals(newRoutine.copy(id = insertId), insertedRoutine)

        val changedRoutine = insertedRoutine.copy(name = "Changed name", restDuration = 5.minutes)

        dao.upsert(changedRoutine).also { updateId ->
          assertEquals(-1L, updateId)

          dao.getRoutine(insertId).also { dbRoutine ->
            checkNotNull(dbRoutine)
            assertEquals(changedRoutine, dbRoutine)
          }
        }
      }
    }
  }

  @Test(expected = SQLiteConstraintException::class)
  fun `schedule upsert throws exception if routine does not exist`() = runTest {
    val newSchedule = RoutineSchedule(id = 0, routineId = 1)

    dao.upsert(newSchedule)
  }

  @Test
  fun `insert and update routine schedule using upsert`() = runTest {
    val newRoutine = Routine(id = 0, name = "New routine", 1.minutes)

    dao.upsert(newRoutine).also { routineId ->
      val newSchedule =
        RoutineSchedule(id = 0, routineId = routineId, sunday = true, wednesday = true)

      dao.upsert(newSchedule).also { insertId ->
        assert(insertId != -1L)
        assert(insertId != 0L)

        dao.getSchedule(insertId).also { insertedSchedule ->
          checkNotNull(insertedSchedule)
          assertEquals(newSchedule.copy(id = insertId), insertedSchedule)

          val changedSchedule = insertedSchedule.copy(sunday = false, monday = true)

          dao.upsert(changedSchedule).also { updateId ->
            assertEquals(-1L, updateId)

            dao.getSchedule(insertId).also { dbSchedule ->
              checkNotNull(dbSchedule)
              assertEquals(changedSchedule, dbSchedule)
            }
          }
        }
      }
    }
  }

  @Test
  fun `insert and update routine with schedule using upsert`() = runTest {
    var routine = Routine(id = 0, name = "New routine", 1.minutes)
    var schedule = RoutineSchedule(id = 0, routineId = 0, sunday = true)

    // Insert
    dao.upsert(RoutineWithSchedule(routine = routine, schedule = schedule)).also { (rId, sId) ->
      assertEquals(1L, rId)
      assertEquals(1L, sId)
      checkNotNull(sId)

      routine = routine.copy(id = rId)
      schedule = schedule.copy(id = sId, routineId = rId)

      // Get
      checkNotNull(dao.getRoutineWithSchedule(routine.id)).also { (dbRoutine, dbSchedule) ->
        checkNotNull(dbSchedule)
        assertEquals(routine, dbRoutine)
        assertEquals(schedule, dbSchedule)

        routine = routine.copy(name = "Updated name", restDuration = 5.minutes)
        schedule = schedule.copy(monday = true, tuesday = true, sunday = false)

        // Update
        dao.upsert(RoutineWithSchedule(routine = routine, schedule = schedule))
          .also { (uRId, uSId) ->
            assertEquals(routine.id, uRId)
            assertEquals(schedule.id, uSId)

            // Get
            checkNotNull(dao.getRoutineWithSchedule(routine.id)).also { (uDbRoutine, uDbSchedule) ->
                checkNotNull(uDbSchedule)
                assertEquals(routine, uDbRoutine)
                assertEquals(schedule, uDbSchedule)
              }
          }
      }
    }
  }

  @Test
  fun `get schedule by routine id`() = runTest {
    val newRoutine = Routine(id = 0, name = "New routine", 1.minutes)

    dao.upsert(newRoutine).also { routineId ->
      val newSchedule = RoutineSchedule(id = 0, routineId = routineId)

      dao.upsert(newSchedule).also { scheduleId ->
        dao.getScheduleByRoutineId(routineId).also { dbSchedule ->
          checkNotNull(dbSchedule)
          assertEquals(newSchedule.copy(id = scheduleId, routineId = routineId), dbSchedule)
        }
      }
    }
  }
}