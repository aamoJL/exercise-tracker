package com.aamo.exercisetracker.ui_tests.features.exercise.form.exercise_form_screen.new_exercise

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
import org.robolectric.annotation.Config

@Suppress("HardCodedStringLiteral")
@Config(qualifiers = "w1000dp-h1000dp-480dpi")
@RunWith(RobolectricTestRunner::class)
class Navigation : PageTest() {
  @Before
  override fun setup() = runTest {
    super.setup()
    toExerciseFormScreen()
    waitForLoading()
  }

  @Test
  fun `on exerciseFormScreen`() = runTest {
    rule.onNodeWithText(getString(R.string.title_new_exercise)).assertExists()
  }

  @Test
  fun back() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()
    rule.onNodeWithContentDescription(getString(R.string.cd_add_exercise)).assertExists()
  }

  @Test
  fun `unsavedChanges dialog cancel`() = runTest {
    rule.onNodeWithText(getString(R.string.label_name)).performTextReplacement("Name")
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()

    rule.onNodeWithText(getString(R.string.dialog_title_unsaved_changes)).assertExists()
    rule.onNodeWithText(getString(R.string.btn_cancel)).performClick()

    rule.onNodeWithText(getString(R.string.title_new_exercise)).assertExists()
  }

  @Test
  fun `unsavedChanges dialog confirm`() = runTest {
    rule.onNodeWithText(getString(R.string.label_name)).performTextReplacement("Name")
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()

    rule.onNodeWithText(getString(R.string.dialog_title_unsaved_changes)).assertExists()
    rule.onNodeWithText(getString(R.string.btn_yes)).performClick()

    rule.onNodeWithContentDescription(getString(R.string.cd_add_exercise)).assertExists()
  }
}