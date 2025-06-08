package com.aamo.exercisetracker.utility.extensions.date

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class DayTests {
  @Test
  fun `getDayNumber returns correct number`() {
    Day.entries.forEachIndexed { i, entry ->
      assertEquals(i + 1, entry.getDayNumber())
    }
  }

  @Test
  fun `getByDayNumber returns correct day`() {
    (1..7).forEach { i ->
      assertEquals(Day.entries[i - 1], Day.getByDayNumber(i))
    }
  }
}

class CalendarExtensionsTests {
  @Test
  fun `getLocalDayOrder returns ordered list of days`() {
    Calendar.getInstance().let { calendar ->
      calendar.let {
        (1..7).forEach { dayNumber ->
          calendar.firstDayOfWeek = dayNumber

          assertArrayEquals(
            (0..6).map { (it + calendar.firstDayOfWeek - 1).mod(7) + 1 }.toTypedArray(),
            calendar.getLocalDayOrder().map { it.getDayNumber() }.toTypedArray()
          )
        }
      }
    }
  }
}