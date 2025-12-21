@file:Suppress("HardCodedStringLiteral")

package com.aamo.exercisetracker.tests.features.routine.form.use_cases

import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.features.routine.form.use_cases.fetchRoutineWithSchedule
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class FetchRoutine : DatabaseTest() {
  @Test
  fun `returns correct model when new`() = runTest {
    val expected = RoutineWithSchedule(routine = Routine(), schedule = null)
    val actual = fetchRoutineWithSchedule(dao = routineDao, routineId = 0L)

    assertEquals(expected, actual)
  }

  @Test
  fun `returns correct model when existing`() = runTest {
    val routine = Routine(name = "Routine 1").let {
      routineDao.upsert(it).let { id -> it.copy(id = id) }
    }
    val schedule = RoutineSchedule(routineId = routine.id, sunday = true).let {
      routineDao.upsert(it).let { id -> it.copy(id = id) }
    }
    val expected = RoutineWithSchedule(routine = routine, schedule = schedule)
    val actual = fetchRoutineWithSchedule(dao = routineDao, routineId = routine.id)

    assertEquals(expected, actual)
  }
}