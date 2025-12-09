package com.aamo.exercisetracker.ui_tests.ui.components

import androidx.compose.material3.Button
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.aamo.exercisetracker.test_utility.ui.TestTags
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.utility.tags.UITag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LoadingScreen {
  @get:Rule val rule = createComposeRule()

  @Test
  fun `content not visible when loading`() {
    rule.setContent {
      LoadingScreen(loading = true) {
        Button(onClick = {}, modifier = Modifier.testTag(TestTags.NODE.name)) {}
      }
    }

    rule.onNodeWithTag(TestTags.NODE.name).assertDoesNotExist()
  }

  @Test
  fun `content visible when not loading`() {
    rule.setContent {
      LoadingScreen(loading = false) {
        Button(onClick = {}, modifier = Modifier.testTag(TestTags.NODE.name)) {}
      }
    }

    rule.onNodeWithTag(TestTags.NODE.name).assertExists()
  }

  @Test
  fun `Progress indicator visible when loading`() {
    rule.setContent {
      LoadingScreen(loading = true) {
        Button(onClick = {}) {}
      }
    }

    rule.onNodeWithTag(UITag.PROGRESS_INDICATOR.name).assertExists()
  }
}