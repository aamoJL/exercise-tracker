package com.aamo.exercisetracker.utility.extensions.date

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DurationTests {
  @Test
  fun `toClockString returns correct string`() {
    assertEquals("00:55", 55.seconds.toClockString())
    assertEquals("00:05", 5.seconds.toClockString())
    assertEquals("01:00", 60.seconds.toClockString())
    assertEquals("02:00", 2.minutes.toClockString())
    assertEquals("10:00", 10.minutes.toClockString())
    assertEquals("00:00", 1.hours.toClockString()) // Hours disabled by default
    assertEquals("01:00:00", 1.hours.toClockString(hasHours = true))
    assertEquals("14:00:00", 14.hours.toClockString(hasHours = true))
    assertEquals("02:00:00", 26.hours.toClockString(hasHours = true))
    assertEquals("01:02:33", (1.hours + 2.minutes + 33.seconds).toClockString(hasHours = true))
  }

  @Test
  fun `weeks returns correct duration`() {
    assertEquals(7L, 1.weeks.inWholeDays)
    assertEquals(14L, 2.weeks.inWholeDays)
    assertEquals(1814400000L, 3.weeks.inWholeMilliseconds)
  }
}