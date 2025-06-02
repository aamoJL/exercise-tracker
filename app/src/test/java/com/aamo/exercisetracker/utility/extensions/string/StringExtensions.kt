package com.aamo.exercisetracker.utility.extensions.string

import org.junit.Assert.assertTrue
import org.junit.Test

class StringExtensionsTests {
  @Test
  fun `empty returns empty string`() {
    assertTrue(String.EMPTY.isEmpty())
  }
}