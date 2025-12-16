package com.aamo.exercisetracker.ui_tests.ui.features.progress_tracking.form.tracked_progress_form_screen.new_tracked_progress

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.test_utility.ui.extensions.assertEditableText
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Suppress("HardCodedStringLiteral")
@Config(qualifiers = "w1000dp-h1000dp-480dpi")
@RunWith(RobolectricTestRunner::class)
class Init : PageTest() {
  @Before
  override fun setup() = runTest {
    super.setup()
    toTrackedProgressFormScreen()
    waitForLoading()
  }

  @Test
  fun `delete button hidden`() {
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_tracked_progress))
      .assertDoesNotExist()
  }

  @Test
  fun `form fields`() {
    rule.onNodeWithText(getString(R.string.label_name)).assertEditableText(String.EMPTY)
    rule.onNodeWithText(getString(R.string.label_weekly_interval_optional))
      .assertEditableText(String.EMPTY)
    rule.onNodeWithText(getString(R.string.label_progress_unit))
      .assertEditableText(getString(R.string.default_repetitions_unit))
    rule.onNodeWithText(getString(R.string.label_minutes)).assertEditableText(String.EMPTY)
      .assertIsNotEnabled()
    rule.onNodeWithText(getString(R.string.label_seconds)).assertEditableText(String.EMPTY)
      .assertIsNotEnabled()
    rule.onNodeWithText(getString(R.string.label_repetitions)).assertIsSelected()
    rule.onNodeWithText(getString(R.string.label_timer)).assertIsNotSelected()
    rule.onNodeWithText(getString(R.string.label_stopwatch)).assertIsNotSelected()
  }
}