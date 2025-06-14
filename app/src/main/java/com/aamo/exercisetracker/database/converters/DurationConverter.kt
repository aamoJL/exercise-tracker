package com.aamo.exercisetracker.database.converters

import androidx.room.TypeConverter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class DurationConverter {
  @TypeConverter
  fun millisecondsToDuration(value: Long?): Duration {
    return value?.milliseconds ?: 0.toDuration(DurationUnit.MILLISECONDS)
  }

  @TypeConverter
  fun durationToMilliseconds(value: Duration?): Long {
    return value?.inWholeMilliseconds ?: 0
  }
}