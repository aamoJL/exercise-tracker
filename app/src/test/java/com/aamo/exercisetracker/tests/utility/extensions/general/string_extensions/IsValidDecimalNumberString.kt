package com.aamo.exercisetracker.tests.utility.extensions.general.string_extensions

import com.aamo.exercisetracker.utility.extensions.general.isValidDecimalNumberString
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

@Suppress("HardCodedStringLiteral")
class IsValidDecimalNumberString {
  @Test
  fun `Test with a valid positive integer string`() {
    assertTrue("123".isValidDecimalNumberString())
  }

  @Test
  fun `Test with a valid negative integer string`() {
    assertTrue("-456".isValidDecimalNumberString())
  }

  @Test
  fun `Test with a string containing zero`() {
    assertTrue("0".isValidDecimalNumberString())
  }

  @Test
  fun `Test with a valid positive decimal string`() {
    assertTrue("123.45".isValidDecimalNumberString())
  }

  @Test
  fun `Test with a valid negative decimal string`() {
    assertTrue("-67.89".isValidDecimalNumberString())
  }

  @Test
  fun `Test with a decimal string starting with a dot`() {
    assertTrue(".5".isValidDecimalNumberString())
  }

  @Test
  fun `Test with a negative decimal string starting with a dot`() {
    assertTrue("-.5".isValidDecimalNumberString())
  }

  @Test
  fun `Test with an integer string ending with a dot`() {
    assertTrue("123.".isValidDecimalNumberString())
  }

  @Test
  fun `Test with an empty string`() {
    assertTrue("".isValidDecimalNumberString())
  }

  @Test
  fun `Test with a string containing only a minus sign`() {
    assertTrue("-".isValidDecimalNumberString())
  }

  @Test
  fun `Test with a string containing only a dot`() {
    assertTrue(".".isValidDecimalNumberString())
  }

  @Test
  fun `Test with a string containing only a minus sign and a dot`() {
    assertTrue("-.".isValidDecimalNumberString())
  }

  @Test
  fun `Test with a non numeric string`() {
    assertFalse("abc".isValidDecimalNumberString())
  }

  @Test
  fun `Test with an alphanumeric string`() {
    assertFalse("1a2b3c".isValidDecimalNumberString())
  }

  @Test
  fun `Test with a string containing multiple decimal points`() {
    assertFalse("12.34.56".isValidDecimalNumberString())
  }

  @Test
  fun `Test with a string containing a misplaced minus sign`() {
    assertFalse("12-3".isValidDecimalNumberString())
  }

  @Test
  fun `Test with a string containing multiple minus signs`() {
    assertFalse("--123".isValidDecimalNumberString())
  }

  @Test
  fun `Test with a string containing spaces`() {
    assertFalse(" 123 ".isValidDecimalNumberString())
  }

  @Test
  fun `Test with a string containing a plus sign`() {
    assertFalse("+123".isValidDecimalNumberString())
  }

  @Test
  fun `Test with a very large number string`() {
    assertTrue("99999999999999999999999999999999999999".isValidDecimalNumberString())
  }

  @Test
  fun `Test with a very long decimal string`() {
    assertTrue("-1.99999999999999999999999999999999999999".isValidDecimalNumberString())
  }
}