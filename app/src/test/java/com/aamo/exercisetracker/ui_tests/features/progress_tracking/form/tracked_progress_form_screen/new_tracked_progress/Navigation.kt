@file:Suppress("HardCodedStringLiteral")

package com.aamo.exercisetracker.ui_tests.features.progress_tracking.form.tracked_progress_form_screen.new_tracked_progress

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Navigation : PageTest() {
  @Before
  override fun setup() = runTest {
    super.setup()
    toTrackedProgressFormScreen()
    waitForLoading()
  }

  @Test
  fun `on trackedProgressFormScreen`() = runTest {
    rule.onNodeWithText(getString(R.string.title_new_tracked_progress)).assertExists()
  }

  @Test
  fun back() {
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()
    rule.onNodeWithText(getString(R.string.ph_search)).assertExists()
  }

  @Test
  fun `unsavedChanges dialog cancel`() {
    rule.onNodeWithText(getString(R.string.label_name)).performTextReplacement("Name")
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()

    rule.onNodeWithText(getString(R.string.dialog_title_unsaved_changes)).assertExists()
    rule.onNodeWithText(getString(R.string.btn_cancel)).performClick()

    rule.onNodeWithText(getString(R.string.title_new_tracked_progress)).assertExists()
  }

  @Test
  fun `unsavedChanges dialog confirm`() {
    rule.onNodeWithText(getString(R.string.label_name)).performTextReplacement("Name")
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()

    rule.onNodeWithText(getString(R.string.dialog_title_unsaved_changes)).assertExists()
    rule.onNodeWithText(getString(R.string.btn_yes)).performClick()

    rule.onNodeWithText(getString(R.string.ph_search)).assertExists()
  }
}