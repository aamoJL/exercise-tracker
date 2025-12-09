package com.aamo.exercisetracker.ui_tests.ui.components.inputs

import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.aamo.exercisetracker.MainActivity
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.ui.components.inputs.BackNavigationIconButton
import com.aamo.exercisetracker.utility.tags.UITag
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BackNavigationIconButton {
  @get:Rule val rule = createAndroidComposeRule<MainActivity>()

  @Test
  fun `Click action triggers onBack callback`() {
    var onBackCalled = false
    rule.activity.setContent {
      BackNavigationIconButton(onBack = { onBackCalled = true })
    }

    rule.onNodeWithTag(UITag.BACK_BUTTON.name).performClick()

    assertTrue(onBackCalled)
  }

  @Test
  fun `Icon presence and content description`() {
    rule.activity.setContent {
      BackNavigationIconButton(onBack = { })
    }

    rule.onNodeWithTag(UITag.BACK_BUTTON.name).assertExists()
    rule.onNodeWithContentDescription(rule.activity.getString(R.string.cd_navigate_back))
      .assertExists()
  }

  @Test
  fun `Component is enabled by default`() {
    rule.activity.setContent {
      BackNavigationIconButton(onBack = { })
    }

    rule.onNodeWithTag(UITag.BACK_BUTTON.name).assertIsEnabled()
  }

  @Test
  fun `Recomposition with different callback`() {
    var callback1Called = false
    var callback2Called = false
    var onBack by mutableStateOf({ callback1Called = true })

    rule.activity.setContent {
      BackNavigationIconButton(onBack = onBack)
    }

    rule.onNodeWithTag(UITag.BACK_BUTTON.name).performClick()
    assertTrue(callback1Called)

    onBack = { callback2Called = true }
    rule.waitForIdle()

    rule.onNodeWithTag(UITag.BACK_BUTTON.name).performClick()
    assertTrue(callback2Called)
  }
}