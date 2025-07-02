package com.aamo.exercisetracker.utility.extensions.date

import org.junit.Assert.assertEquals
import org.junit.Test
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
  }
}