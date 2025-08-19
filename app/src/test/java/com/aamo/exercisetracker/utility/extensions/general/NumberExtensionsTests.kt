package com.aamo.exercisetracker.utility.extensions.general

import junit.framework.TestCase.assertEquals
import org.junit.Test

class NumberExtensionsTests {
  @Test
  fun `digits returns the correct amount of digits`() {
    assertEquals(1, 0.digits())
    assertEquals(1, 1.digits())
    assertEquals(1, 9.digits())
    assertEquals(2, 10.digits())
    assertEquals(2, 99.digits())
    assertEquals(3, 100.digits())
    assertEquals(3, 999.digits())
    assertEquals(9, 123456789.digits())
  }
}