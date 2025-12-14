package com.aamo.exercisetracker.ui_tests.ui.features.routine.form.routine_form_screen.new_routine

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.test_utility.ui.extensions.assertEditableText
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import com.aamo.exercisetracker.utility.extensions.date.Day
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
    toRoutineFormScreen()
    waitForLoading()
  }

  @Test
  fun `delete button hidden`() {
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_routine)).assertDoesNotExist()
  }

  @Test
  fun `form fields`() {
    rule.onNodeWithText(getString(R.string.label_name)).assertEditableText(String.EMPTY)

    Day.entries.forEach { day ->
      rule.onNodeWithText(getString(day.nameResourceKey).take(2)).assertIsOff()
    }
  }
}