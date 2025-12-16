package com.aamo.exercisetracker.ui_tests.ui.features.exercise.form.exercise_form_screen

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.test_utility.ui.extensions.assertEditableText
import com.aamo.exercisetracker.test_utility.ui.extensions.onAllNodesWithEditableText
import com.aamo.exercisetracker.test_utility.ui.extensions.onNodeWithEditableText
import com.aamo.exercisetracker.test_utility.ui.extensions.waitForNotDisplayed
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
class ListItems : PageTest() {
  lateinit var set: ExerciseSet

  @Before
  override fun setup() = runTest {
    super.setup()
    set = toExerciseFormScreen().sets.first()
    waitForLoading()
  }

  @Test
  fun `add new item`() = runTest {
    rule.onAllNodesWithEditableText("0").assertCountEquals(1)
    rule.onNodeWithContentDescription(getString(R.string.cd_add_new_item)).assertExists()
      .performClick()
    rule.onAllNodesWithEditableText("0").assertCountEquals(2)
  }

  @Test
  fun `remove item`() = runTest {
    rule.onNodeWithEditableText("0").assertExists().performTouchInput { swipeRight() }
    rule.onNodeWithEditableText("0").assertDoesNotExist()
  }

  @Test
  fun `duration field if timer is enabled`() = runTest {
    rule.onAllNodesWithEditableText("00").assertCountEquals(0)
    rule.onNodeWithEditableText("0").assertExists()
    rule.onNodeWithText(getString(R.string.label_timer)).performClick()
    rule.onNodeWithEditableText("0").waitForNotDisplayed().assertDoesNotExist()
    rule.onAllNodesWithEditableText("00").assertCountEquals(2)
  }

  @Test
  fun `unit field disabled when timer is enabled`() = runTest {
    rule.onNodeWithText(getString(R.string.label_set_unit)).assertIsEnabled()
    rule.onNodeWithText(getString(R.string.label_set_unit)).assertEditableText(set.unit)
    rule.onNodeWithText(getString(R.string.label_timer)).performClick()
    rule.onNodeWithText(getString(R.string.label_set_unit)).assertIsNotEnabled()
    rule.onNodeWithText(getString(R.string.label_set_unit)).assertEditableText(String.EMPTY)
  }
}