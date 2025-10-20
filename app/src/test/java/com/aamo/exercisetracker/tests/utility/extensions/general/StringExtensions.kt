package com.aamo.exercisetracker.tests.utility.extensions.general

import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import org.junit.Assert.assertTrue
import org.junit.Test

class StringExtensions {
  @Test
  fun `empty returns empty string`() {
    assertTrue(String.Companion.EMPTY.isEmpty())
  }
}