package com.aamo.exercisetracker.tests.database.entities

import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.utility.extensions.date.Day
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class Routine {
  @Test
  fun `isDaySelected test`() {
    assertTrue(RoutineSchedule(routineId = 0L, sunday = true).isDaySelected(1))
    assertTrue(RoutineSchedule(routineId = 0L, monday = true).isDaySelected(2))
    assertTrue(RoutineSchedule(routineId = 0L, tuesday = true).isDaySelected(3))
    assertTrue(RoutineSchedule(routineId = 0L, wednesday = true).isDaySelected(4))
    assertTrue(RoutineSchedule(routineId = 0L, thursday = true).isDaySelected(5))
    assertTrue(RoutineSchedule(routineId = 0L, friday = true).isDaySelected(6))
    assertTrue(RoutineSchedule(routineId = 0L, saturday = true).isDaySelected(7))
  }

  @Test
  fun `asListOfDays test`() {
    assertArrayEquals(
      listOf(Day.SUNDAY, Day.MONDAY, Day.WEDNESDAY).toTypedArray(),
      RoutineSchedule(routineId = 0L, monday = true, wednesday = true, sunday = true).asListOfDays()
        .toTypedArray()
    )
  }
}