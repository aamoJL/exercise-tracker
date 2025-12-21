package com.aamo.exercisetracker.tests.features.routine.form.use_cases

import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.features.routine.form.use_cases.saveRoutine
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class SaveRoutine() : DatabaseTest() {
  @Test
  fun `returns correct id when saved`() = runTest {
    val routine = Routine(name = "Routine 1")
    val schedule = RoutineSchedule(routineId = 0L, sunday = true)

    val result = saveRoutine(dao = routineDao, routine = routine, schedule = schedule)
    val actual = routineDao.getRoutineWithSchedule(routineId = result)

    checkNotNull(actual)
    assertEquals(routine.name, actual.routine.name)
    assertEquals(schedule.asListOfDays(), actual.schedule?.asListOfDays())
  }
}