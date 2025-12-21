package com.aamo.exercisetracker.tests.utility.extensions.general.string_extensions

import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import org.junit.Assert
import org.junit.Test

class Empty {
  @Test
  fun `empty returns empty string`() {
    Assert.assertTrue(String.EMPTY.isEmpty())
  }
}