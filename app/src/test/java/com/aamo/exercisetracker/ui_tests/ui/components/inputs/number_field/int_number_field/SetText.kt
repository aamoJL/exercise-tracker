@file:Suppress("HardCodedStringLiteral")

package com.aamo.exercisetracker.ui_tests.ui.components.inputs.number_field.int_number_field

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextReplacement
import com.aamo.exercisetracker.test_utility.ui.TestTags
import com.aamo.exercisetracker.ui.components.inputs.number_field.IntFieldValidator
import com.aamo.exercisetracker.ui.components.inputs.number_field.NumberField
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SetText {
  @get:Rule val rule = createComposeRule()

  private var value by mutableStateOf(0)

  private fun assertText(text: String) {
    rule.onNodeWithTag(TestTags.NODE.name).assert(hasText(text))
  }

  private fun assertValue(value: Int) {
    assertEquals(value, this.value)
  }

  private fun replaceText(text: String) {
    rule.onNodeWithTag(TestTags.NODE.name).performTextReplacement(text = text)
  }

  private fun setup(onValueChange: ((Int) -> Unit)? = null) {
    rule.setContent {
      NumberField(
        value = value,
        onValueChange = { value = it; onValueChange?.invoke(it) },
        validator = IntFieldValidator,
        modifier = Modifier.testTag(TestTags.NODE.name)
      )
    }
  }

  @Test
  fun `setup sanity`() {
    var onValueChangeCalled = false
    setup(onValueChange = { onValueChangeCalled = true })
    replaceText("1")
    assertTrue(onValueChangeCalled)
  }

  @Test
  fun `readOnly true`() {
    val expected = 0 to "0"
    rule.setContent {
      NumberField(
        value = expected.first,
        onValueChange = { fail() },
        validator = IntFieldValidator,
        readOnly = true,
        modifier = Modifier.testTag(TestTags.NODE.name)
      )
    }
    rule.waitForIdle()

    assertText(expected.second)
    assertThrows(AssertionError::class.java) {
      replaceText("5")
    }
    assertText(expected.second)
    assertEquals(expected.first, value)
  }

  @Test
  fun `input same`() {
    setup { fail() }
    replaceText(value.toString())
  }

  @Test
  fun `input valid text`() {
    setup()

    val inputOutputs = listOf(
      "100" to ("100" to 100),
      "-100" to ("-100" to -100),
      "2147483647" to ("2147483647" to Int.MAX_VALUE),
      "-2147483647" to ("-2147483647" to -Int.MAX_VALUE),
      "-2147483648" to ("-2147483648" to Int.MIN_VALUE),
      String.EMPTY to ("0" to 0),
      "0" to ("0" to 0),
      "000" to ("0" to 0),
      "00100" to ("100" to 100),
      "0-" to ("-" to 0),
      "-" to ("-" to 0),
      "0-2" to ("-2" to -2),
    )

    inputOutputs.forEach { (input, outputs) ->
      replaceText(input)
      assertText(outputs.first)
      assertValue(outputs.second)
      rule.onNodeWithTag(TestTags.NODE.name).performTextClearance()
    }
  }

  @Test
  fun `input invalid text`() {
    setup { fail() }

    val inputs = listOf(
      "1.99999",
      "-1.99999",
      "0.0",
      "000.000",
      "0.100",
      "9990282350000000000000000000000000000000",
      "-9990282350000000000000000000000000000000",
      ".",
      ".00",
      "..",
      ".-1",
      "1.0.0",
      "1 2",
      " ",
      "test",
      "12f",
      "--12",
      "-.-12",
      "1-2",
    )

    val expected = "5" to 5

    // sanity check
    assertThrows(AssertionError::class.java) { replaceText(expected.first) }

    inputs.forEach { input ->
      replaceText(input)
      assertText(expected.first)
      assertValue(expected.second)
    }
  }
}