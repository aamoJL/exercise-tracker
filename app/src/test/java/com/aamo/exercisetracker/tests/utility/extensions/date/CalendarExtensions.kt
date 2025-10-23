package com.aamo.exercisetracker.tests.utility.extensions.date

import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOrder
import com.aamo.exercisetracker.utility.extensions.date.toDay
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.util.Calendar

class CalendarExtensions {
  @Test
  fun `getDayNumber returns correct number`() {
    Day.entries.forEachIndexed { i, entry ->
      assertEquals(i + 1, entry.getDayNumber())
    }
  }

  @Test
  fun `getByDayNumber returns correct day`() {
    (1..7).forEach { i ->
      assertEquals(Day.entries[i - 1], Day.Companion.getByDayNumber(i))
    }
  }

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

  @Test
  fun `day of week returns correct day`() {
    assertEquals(Day.SUNDAY, DayOfWeek.SUNDAY.toDay())
    assertEquals(Day.MONDAY, DayOfWeek.MONDAY.toDay())
    assertEquals(Day.TUESDAY, DayOfWeek.TUESDAY.toDay())
    assertEquals(Day.WEDNESDAY, DayOfWeek.WEDNESDAY.toDay())
    assertEquals(Day.THURSDAY, DayOfWeek.THURSDAY.toDay())
    assertEquals(Day.FRIDAY, DayOfWeek.FRIDAY.toDay())
    assertEquals(Day.SATURDAY, DayOfWeek.SATURDAY.toDay())
  }
}