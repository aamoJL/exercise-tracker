package com.aamo.exercisetracker.utility.extensions.date

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class CalendarExtensionsTests {
  @Test
  fun `getLocalDayOfWeek returns converted day number`() {
    Calendar.getInstance().let { calendar ->
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY).let {
        calendar.firstDayOfWeek = Calendar.MONDAY
        assertEquals(7, calendar.getLocalDayOfWeek())
      }

      calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY).let {
        calendar.firstDayOfWeek = Calendar.WEDNESDAY
        assertEquals(6, calendar.getLocalDayOfWeek())
      }

      calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY).let {
        calendar.firstDayOfWeek = Calendar.MONDAY
        assertEquals(4, calendar.getLocalDayOfWeek())
      }

      calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY).let {
        calendar.firstDayOfWeek = Calendar.SATURDAY
        assertEquals(2, calendar.getLocalDayOfWeek())
      }

      calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY).let {
        calendar.firstDayOfWeek = Calendar.WEDNESDAY
        assertEquals(1, calendar.getLocalDayOfWeek())
      }
    }
  }

  @Test
  fun `getLocalListOfDays returns ordered list of days`() {
    Calendar.getInstance().let { calendar ->
      calendar.let {
        calendar.firstDayOfWeek = Calendar.MONDAY

        assertArrayEquals(
          arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),
          calendar.getLocalListOfDays().toTypedArray()
        )
      }

      calendar.let {
        calendar.firstDayOfWeek = Calendar.SUNDAY

        assertArrayEquals(
          arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"),
          calendar.getLocalListOfDays().toTypedArray()
        )
      }

      calendar.let {
        calendar.firstDayOfWeek = Calendar.WEDNESDAY

        assertArrayEquals(
          arrayOf("Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday"),
          calendar.getLocalListOfDays().toTypedArray()
        )
      }
    }
  }
}