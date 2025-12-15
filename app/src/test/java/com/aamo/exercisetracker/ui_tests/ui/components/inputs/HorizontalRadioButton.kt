package com.aamo.exercisetracker.ui_tests.ui.components.inputs

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aamo.exercisetracker.test_utility.ui.TestTags
import com.aamo.exercisetracker.ui.components.inputs.HorizontalRadioButton
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class HorizontalRadioButton {
  @get:Rule val rule = createComposeRule()

  @Test
  fun `title visible`() {
    val title = "Title"
    rule.setContent {
      HorizontalRadioButton(title = title, selected = false, onSelect = { TestCase.fail() })
    }

    rule.onNodeWithText(title).assertExists()
  }

  @Test
  fun selectable() {
    rule.setContent {
      HorizontalRadioButton(title = String.EMPTY, selected = false, onSelect = { TestCase.fail() })
    }
    rule.onNode(isSelectable()).assertExists()
  }

  @Test
  fun selected() {
    rule.setContent {
      HorizontalRadioButton(title = String.EMPTY, selected = true, onSelect = { TestCase.fail() })
    }
    rule.onNode(isSelected()).assertExists()
  }

  @Test
  fun unselected() {
    rule.setContent {
      HorizontalRadioButton(title = String.EMPTY, selected = false, onSelect = { TestCase.fail() })
    }
    rule.onNode(isSelected()).assertDoesNotExist()
  }

  @Test
  fun `onSelect called`() {
    var called = false
    rule.setContent {
      HorizontalRadioButton(
        title = String.Companion.EMPTY,
        selected = false,
        onSelect = { called = true },
        modifier = Modifier.Companion.testTag(TestTags.NODE.name)
      )
    }

    rule.onNodeWithTag(TestTags.NODE.name).performClick()
    TestCase.assertTrue(called)
  }
}