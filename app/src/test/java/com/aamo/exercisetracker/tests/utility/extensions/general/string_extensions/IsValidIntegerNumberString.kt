@file:Suppress("HardCodedStringLiteral")

package com.aamo.exercisetracker.tests.utility.extensions.general.string_extensions

import com.aamo.exercisetracker.utility.extensions.general.isValidIntegerString
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class IsValidIntegerNumberString {
  @Test
  fun `Test with a valid positive integer string`() {
    assertTrue("123".isValidIntegerString())
  }

  @Test
  fun `Test with a valid negative integer string`() {
    assertTrue("-456".isValidIntegerString())
  }

  @Test
  fun `Test with a string containing zero`() {
    assertTrue("0".isValidIntegerString())
  }

  @Test
  fun `Test with a valid positive decimal string`() {
    assertFalse("123.45".isValidIntegerString())
  }

  @Test
  fun `Test with a valid negative decimal string`() {
    assertFalse("-67.89".isValidIntegerString())
  }

  @Test
  fun `Test with a decimal string starting with a dot`() {
    assertFalse(".5".isValidIntegerString())
  }

  @Test
  fun `Test with a negative decimal string starting with a dot`() {
    assertFalse("-.5".isValidIntegerString())
  }

  @Test
  fun `Test with an integer string ending with a dot`() {
    assertFalse("123.".isValidIntegerString())
  }

  @Test
  fun `Test with an empty string`() {
    assertTrue("".isValidIntegerString())
  }

  @Test
  fun `Test with a string containing only a minus sign`() {
    assertTrue("-".isValidIntegerString())
  }

  @Test
  fun `Test with a string containing only a dot`() {
    assertFalse(".".isValidIntegerString())
  }

  @Test
  fun `Test with a string containing only a minus sign and a dot`() {
    assertFalse("-.".isValidIntegerString())
  }

  @Test
  fun `Test with a non numeric string`() {
    assertFalse("abc".isValidIntegerString())
  }

  @Test
  fun `Test with an alphanumeric string`() {
    assertFalse("1a2b3c".isValidIntegerString())
  }

  @Test
  fun `Test with a string containing multiple decimal points`() {
    assertFalse("12.34.56".isValidIntegerString())
  }

  @Test
  fun `Test with a string containing a misplaced minus sign`() {
    assertFalse("12-3".isValidIntegerString())
  }

  @Test
  fun `Test with a string containing multiple minus signs`() {
    assertFalse("--123".isValidIntegerString())
  }

  @Test
  fun `Test with a string containing spaces`() {
    assertFalse(" 123 ".isValidIntegerString())
  }

  @Test
  fun `Test with a string containing a plus sign`() {
    assertFalse("+123".isValidIntegerString())
  }

  @Test
  fun `Test with a very large number string`() {
    assertTrue("99999999999999999999999999999999999999".isValidIntegerString())
  }

  @Test
  fun `Test with a very long decimal string`() {
    assertFalse("-1.99999999999999999999999999999999999999".isValidIntegerString())
  }
}