package com.aamo.exercisetracker.ui_tests.ui.components.inputs

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import com.aamo.exercisetracker.test_utility.ui.TestTags
import com.aamo.exercisetracker.ui.components.inputs.LoadingIconButton
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import com.aamo.exercisetracker.utility.tags.UITag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LoadingIconButton {
  @get:Rule val rule = createComposeRule()

  @Test
  fun `content visible`() {
    rule.setContent {
      LoadingIconButton(onClick = {}, isLoading = true, enabled = true) {
        Text(text = String.EMPTY, modifier = Modifier.testTag(TestTags.NODE.name))
      }
    }

    rule.onNode(hasTestTag(TestTags.NODE.name), useUnmergedTree = true).assertExists()
  }

  @Test
  fun `progress indicator visibility`() {
    var loading by mutableStateOf(false)
    rule.setContent {
      LoadingIconButton(onClick = {}, isLoading = loading, enabled = true) {}
    }

    rule.onNodeWithTag(UITag.PROGRESS_INDICATOR.name).assertDoesNotExist()

    loading = true

    rule.onNodeWithTag(UITag.PROGRESS_INDICATOR.name).assertExists()
  }

  @Test
  fun `button enable state`() {
    var loading by mutableStateOf(false)
    var enabled by mutableStateOf(false)
    rule.setContent {
      LoadingIconButton(onClick = {}, isLoading = loading, enabled = enabled) {
        Text(text = String.EMPTY, modifier = Modifier.testTag(TestTags.NODE.name))
      }
    }

    val node = rule.onNode(hasTestTag(TestTags.NODE.name), useUnmergedTree = true).onParent()

    // false, false
    node.assertIsNotEnabled()
    enabled = true
    // false, true
    node.assertIsEnabled()
    loading = true
    //true, true
    node.assertIsNotEnabled()
    enabled = false
    // true, false
    node.assertIsNotEnabled()
  }

  @Test
  fun `onClick called`() {
    var called = false
    rule.setContent {
      LoadingIconButton(onClick = { called = true }, isLoading = false, enabled = true) {
        Text(text = String.EMPTY, modifier = Modifier.testTag(TestTags.NODE.name))
      }
    }

    rule.onNode(hasTestTag(TestTags.NODE.name), useUnmergedTree = true).onParent().performClick()

    assert(called)
  }
}