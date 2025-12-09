@file:Suppress("HardCodedStringLiteral")

package com.aamo.exercisetracker.ui_tests.ui.components.inputs.text_field

import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.aamo.exercisetracker.MainActivity
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.test_utility.ui.TestTags
import com.aamo.exercisetracker.ui.components.inputs.text_field.SearchTextField
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchTextField {
  @get:Rule val rule = createAndroidComposeRule<MainActivity>()

  @Test
  fun `placeholder visibility`() {
    var value by mutableStateOf(String.EMPTY)
    val placeholder = "Placeholder"
    rule.activity.setContent {
      SearchTextField(value = value, onValueChange = {}, placeholder = placeholder)
    }

    rule.onNode(hasText(placeholder)).assertExists()

    value = "value"

    rule.onNode(hasText(placeholder)).assertDoesNotExist()
  }

  @Test
  fun `value change`() {
    var value by mutableStateOf("Value")
    rule.activity.setContent {
      SearchTextField(value = value, onValueChange = {})
    }

    rule.onNode(hasText(value)).assertExists()

    value = "New Value"

    rule.onNode(hasText(value)).assertExists()
  }

  @Test
  fun `onValueChange called`() {
    var called = false
    rule.activity.setContent {
      SearchTextField(
        value = String.EMPTY,
        onValueChange = { called = true },
        modifier = Modifier.testTag(TestTags.NODE.name)
      )
    }

    rule.onNodeWithTag(TestTags.NODE.name).performTextInput("Value")

    assert(called)
  }

  @Test
  fun `clear state change`() {
    var value by mutableStateOf(String.EMPTY)
    rule.activity.setContent {
      SearchTextField(value = value, onValueChange = {})
    }

    rule.onNodeWithContentDescription(rule.activity.getString(R.string.cd_clear))
      .assertDoesNotExist()

    value = "New Value"

    rule.onNodeWithContentDescription(rule.activity.getString(R.string.cd_clear)).assertExists()
  }

  @Test
  fun `clear click`() {
    var value by mutableStateOf("Value")
    rule.activity.setContent {
      SearchTextField(value = value, onValueChange = { value = it })
    }

    Assert.assertNotEquals(String.EMPTY, value)

    rule.onNodeWithContentDescription(rule.activity.getString(R.string.cd_clear)).performClick()

    Assert.assertEquals(String.EMPTY, value)
  }
}