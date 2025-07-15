package com.aamo.exercisetracker.database.converters

import com.aamo.exercisetracker.database.entities.ExerciseSet
import org.junit.Assert
import org.junit.Test

class ExerciseSetValueTypeConverterTests {
  @Test
  fun `to returns right value type`() {
    ExerciseSetValueTypeConverter().apply {
      ExerciseSet.ValueType.entries.forEach { type ->
        Assert.assertEquals(type, to(type.id))
      }
      Assert.assertEquals(ExerciseSet.ValueType.REPETITION, to(null))
    }
  }

  @Test
  fun `from returns right value type index`() {
    ExerciseSetValueTypeConverter().apply {
      ExerciseSet.ValueType.entries.forEach { type ->
        Assert.assertEquals(type.id, from(type))
      }
      Assert.assertEquals(ExerciseSet.ValueType.REPETITION.id, from(null))
    }
  }
}