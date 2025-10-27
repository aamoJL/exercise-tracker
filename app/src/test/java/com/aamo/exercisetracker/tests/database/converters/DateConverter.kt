package com.aamo.exercisetracker.tests.database.converters

import com.aamo.exercisetracker.database.converters.DateConverter
import org.junit.Assert
import org.junit.Test
import java.util.Date

class DateConverter {
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