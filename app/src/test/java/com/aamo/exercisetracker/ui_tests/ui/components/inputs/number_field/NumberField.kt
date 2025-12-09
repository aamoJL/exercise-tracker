package com.aamo.exercisetracker.ui_tests.ui.components.inputs.number_field

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.aamo.exercisetracker.test_utility.ui.TestTags
import com.aamo.exercisetracker.ui.components.inputs.number_field.FieldValidator
import com.aamo.exercisetracker.ui.components.inputs.number_field.NumberField
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NumberField {
  data object TestFieldValidator : FieldValidator<Int> {
    override fun onValid(text: String, onValid: (value: Int, text: String) -> Unit) {
      onValid(1, text)
    }

    override fun onValid(value: Int, onValid: (text: String) -> Unit): Boolean {
      onValid(value.toString())
      return true
    }
  }

  @get:Rule val rule = createComposeRule()

  @Test
  fun `onValueChanged called`() {
    var onValueChangedCalled = false
    rule.setContent {
      NumberField(
        value = 0,
        onValueChange = { onValueChangedCalled = true },
        validator = TestFieldValidator,
        modifier = Modifier.testTag(TestTags.NODE.name)
      )
    }
    rule.waitForIdle()

    rule.onNodeWithTag(TestTags.NODE.name).performTextInput(text = "1")

    TestCase.assertTrue(onValueChangedCalled)
  }

  @Test
  fun `enabled false`() {
    rule.setContent {
      NumberField(
        value = 0,
        onValueChange = {},
        validator = TestFieldValidator,
        enabled = false,
        modifier = Modifier.testTag(TestTags.NODE.name)
      )
    }
    rule.waitForIdle()

    rule.onNodeWithTag(TestTags.NODE.name).assertIsNotEnabled()
  }

  @Test
  fun `label visible`() {
    val labelText = "MyLabel"
    rule.setContent {
      NumberField(
        value = 0,
        onValueChange = {},
        validator = TestFieldValidator,
        label = { Text(labelText) },
        modifier = Modifier.testTag(TestTags.NODE.name)
      )
    }
    rule.waitForIdle()
    rule.onNodeWithText(labelText).assertExists()
  }
}