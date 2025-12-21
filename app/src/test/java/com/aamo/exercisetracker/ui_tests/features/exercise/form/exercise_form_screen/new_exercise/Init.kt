package com.aamo.exercisetracker.ui_tests.features.exercise.form.exercise_form_screen.new_exercise

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.test_utility.ui.extensions.assertEditableText
import com.aamo.exercisetracker.test_utility.ui.extensions.onNodeWithEditableText
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Init : PageTest() {
  @Before
  override fun setup() = runTest {
    super.setup()
    toExerciseFormScreen()
    waitForLoading()
  }

  @Test
  fun `delete button hidden`() {
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_exercise)).assertDoesNotExist()
  }

  @Test
  fun `form fields`() {
    rule.onNodeWithText(getString(R.string.label_name)).assertEditableText(String.EMPTY)
    rule.onNodeWithText(getString(R.string.label_rest_duration)).assertIsOff()
    rule.onNodeWithText(getString(R.string.label_minutes)).assertEditableText(String.EMPTY)
    rule.onNodeWithText(getString(R.string.label_seconds)).assertEditableText(String.EMPTY)
    rule.onNodeWithText(getString(R.string.label_set_unit))
      .assertEditableText(getString(R.string.default_repetitions_unit))
    rule.onNodeWithText(getString(R.string.label_timer)).assertExists().assertIsOff()
    rule.onNodeWithEditableText("0").assertExists()
  }
}