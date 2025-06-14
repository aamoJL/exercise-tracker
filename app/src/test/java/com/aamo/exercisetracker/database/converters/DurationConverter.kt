package com.aamo.exercisetracker.database.converters

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class DurationConverterTests {
  @Test
  fun `milliseconds converted to duration correctly`() {
    val value = 1400L
    assertEquals(value.milliseconds, DurationConverter().millisecondsToDuration(value))
    assertEquals(0.milliseconds, DurationConverter().millisecondsToDuration(null))
  }

  @Test
  fun `duration converted to milliseconds correctly`() {
    val value = 1500L
    assertEquals(value, DurationConverter().durationToMilliseconds(value.milliseconds))
    assertEquals(0, DurationConverter().durationToMilliseconds(null))
  }
}