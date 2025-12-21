package com.aamo.exercisetracker.ui_tests.features.exercise.form.exercise_form_screen.new_exercise

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.test_utility.ui.extensions.onAllNodesWithEditableText
import com.aamo.exercisetracker.test_utility.ui.extensions.onNodeWithEditableText
import com.aamo.exercisetracker.test_utility.ui.extensions.waitForDisplayed
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
class Submit : PageTest() {
  @Before
  override fun setup() = runTest {
    super.setup()
    toExerciseFormScreen()
    waitForLoading()
  }

  @Test
  fun submit() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_save_exercise)).assertIsNotEnabled()

    val name = "New Name"
    rule.onNodeWithText(getString(R.string.label_name)).performTextReplacement(name)
    rule.onNodeWithText(getString(R.string.label_rest_duration)).performClick()
    rule.onNodeWithText(getString(R.string.label_minutes)).performTextReplacement("3")

    rule.onNodeWithContentDescription(getString(R.string.cd_add_new_item)).assertExists()
      .performClick()

    rule.onAllNodesWithEditableText("0").assertCountEquals(2)
    rule.onAllNodesWithEditableText("0").onFirst().performTextReplacement("123")
    rule.onNodeWithEditableText("0").performTextReplacement("999")

    rule.onNodeWithContentDescription(getString(R.string.cd_save_exercise)).assertIsEnabled()
      .performClick()
    rule.onNodeWithContentDescription(getString(R.string.cd_edit_routine)).waitForDisplayed()
      .assertExists()
    rule.onNodeWithText(name).assertExists()
  }
}