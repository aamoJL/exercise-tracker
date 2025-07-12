package com.aamo.exercisetracker.database.converters

import androidx.room.TypeConverter
import com.aamo.exercisetracker.database.entities.ExerciseSet

class ExerciseSetValueTypeConverter {
  @TypeConverter
  fun to(typeId: Int?): ExerciseSet.ValueType {
    return ExerciseSet.ValueType.entries.firstOrNull { it.id == typeId }
      ?: ExerciseSet.ValueType.REPETITION
  }

  @TypeConverter
  fun from(type: ExerciseSet.ValueType?): Int {
    return type?.id ?: 0
  }
}