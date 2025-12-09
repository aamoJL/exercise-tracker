package com.aamo.exercisetracker.ui_tests.ui.components.modals

import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.aamo.exercisetracker.MainActivity
import com.aamo.exercisetracker.test_utility.ui.TestTags
import com.aamo.exercisetracker.ui.components.modals.CustomModalBottomSheet
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CustomModalBottomSheet {
  @get:Rule val rule = createAndroidComposeRule<MainActivity>()

  @Test
  fun `Content visible when shown`() {
    rule.activity.setContent {
      CustomModalBottomSheet(show = true, onDismissRequest = {}) {
        Text(String.EMPTY, modifier = Modifier.testTag(TestTags.NODE.name))
      }
    }

    rule.onNodeWithTag(TestTags.NODE.name).assertExists()
  }

  @Test
  fun `Content not visible when not shown`() {
    rule.activity.setContent {
      CustomModalBottomSheet(show = false, onDismissRequest = {}) {
        Text(String.EMPTY, modifier = Modifier.testTag(TestTags.NODE.name))
      }
    }

    rule.onNodeWithTag(TestTags.NODE.name).assertDoesNotExist()
  }
}