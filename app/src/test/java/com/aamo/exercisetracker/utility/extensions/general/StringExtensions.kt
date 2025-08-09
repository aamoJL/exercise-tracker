package com.aamo.exercisetracker.utility.extensions.general

import org.junit.Assert.assertTrue
import org.junit.Test

class StringExtensionsTests {
  @Test
  fun `empty returns empty string`() {
    assertTrue(String.EMPTY.isEmpty())
  }
}