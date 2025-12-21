package com.aamo.exercisetracker.ui_tests.features.routine.form.routine_form_screen.new_routine

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.test_utility.ui.extensions.assertEditableText
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import com.aamo.exercisetracker.utility.extensions.date.Day
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
    toRoutineFormScreen()
    waitForLoading()
  }

  @Test
  fun submit() = runTest {
    val name = "Routine 1"
    val days = listOf(Day.MONDAY, Day.WEDNESDAY)

    rule.onNodeWithContentDescription(getString(R.string.cd_save_routine)).assertIsNotEnabled()

    rule.onNodeWithText(getString(R.string.label_name)).performTextReplacement(name)
    days.forEach {
      rule.onNodeWithText(getString(it.nameResourceKey).take(2)).performClick()
    }

    rule.onNodeWithContentDescription(getString(R.string.cd_save_routine)).assertIsEnabled()
      .performClick()
    waitForLoading()

    rule.onNodeWithContentDescription(getString(R.string.cd_edit_routine)).assertExists()

    // validate submit
    rule.onNodeWithContentDescription(getString(R.string.cd_edit_routine)).performClick()
    waitForLoading()
    rule.onNodeWithText(getString(R.string.label_name)).assertEditableText(name)
    Day.entries.forEach {
      if (days.contains(it)) rule.onNodeWithText(getString(it.nameResourceKey).take(2)).assertIsOn()
      else rule.onNodeWithText(getString(it.nameResourceKey).take(2)).assertIsOff()
    }
  }
}