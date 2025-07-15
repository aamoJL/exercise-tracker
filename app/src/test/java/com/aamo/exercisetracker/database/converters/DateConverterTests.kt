package com.aamo.exercisetracker.database.converters

import org.junit.Assert
import org.junit.Test
import java.util.Date

class DateConverterTests {
  @Test
  fun `from timestamp returns right date`() {
    val timestamp = System.currentTimeMillis()
    Assert.assertEquals(Date(timestamp), DateConverter().fromTimestamp(timestamp))
    Assert.assertEquals(null, DateConverter().fromTimestamp(null))
  }

  @Test
  fun `date to timestamp returns right timestamp`() {
    val date = Date(System.currentTimeMillis())
    Assert.assertEquals(date.time, DateConverter().dateToTimestamp(date))
    Assert.assertEquals(null, DateConverter().dateToTimestamp(null))
  }
}