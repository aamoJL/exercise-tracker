package com.aamo.exercisetracker.ui_tests.features.progress_tracking.form.tracked_progress_form_screen.new_tracked_progress

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
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

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Submit : PageTest() {
  @Before
  override fun setup() = runTest {
    super.setup()
    toTrackedProgressFormScreen()
    waitForLoading()
  }

  @Test
  fun submit() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_save_tracked_progress))
      .assertIsNotEnabled()

    rule.onNodeWithText(getString(R.string.label_name)).performTextReplacement("Progress 1")

    rule.onNodeWithContentDescription(getString(R.string.cd_save_tracked_progress))
      .assertIsEnabled().performClick()
    waitForLoading()

    rule.onNodeWithText(getString(R.string.ph_search)).assertExists()
  }
}