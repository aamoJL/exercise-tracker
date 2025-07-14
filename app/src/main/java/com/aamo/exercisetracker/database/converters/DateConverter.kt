package com.aamo.exercisetracker.database.converters

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {
  @TypeConverter
  fun fromTimestamp(millis: Long?): Date? {
    return millis?.let { Date(it) }
  }

  /**
   * @return timestamp in milliseconds
   */
  @TypeConverter
  fun dateToTimestamp(date: Date?): Long? {
    return date?.time
  }
}