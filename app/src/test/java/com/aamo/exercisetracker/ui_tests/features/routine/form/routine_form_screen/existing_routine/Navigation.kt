@file:Suppress("HardCodedStringLiteral")

package com.aamo.exercisetracker.ui_tests.features.routine.form.routine_form_screen.existing_routine

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Navigation : PageTest() {
  lateinit var model: RoutineWithSchedule

  @Before
  override fun setup() = runTest {
    super.setup()
    model = toRoutineFormScreen(
      model = RoutineWithSchedule(
        routine = Routine(name = "Routine 1"),
        schedule = RoutineSchedule(routineId = 0L, sunday = true)
      )
    )
    waitForLoading()
  }

  @Test
  fun `on routineFormScreen`() = runTest {
    rule.onNodeWithText(getString(R.string.title_existing_routine)).assertExists()
  }

  @Test
  fun back() {
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()
    rule.onNodeWithContentDescription(getString(R.string.cd_edit_routine)).assertExists()
  }

  @Test
  fun `unsavedChanges dialog cancel`() {
    rule.onNodeWithText(getString(R.string.label_name)).performTextReplacement("New Name")
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()

    rule.onNodeWithText(getString(R.string.dialog_title_unsaved_changes)).assertExists()
    rule.onNodeWithText(getString(R.string.btn_cancel)).performClick()

    rule.onNodeWithText(getString(R.string.title_existing_routine)).assertExists()
  }

  @Test
  fun `unsavedChanges dialog confirm`() {
    rule.onNodeWithText(getString(R.string.label_name)).performTextReplacement("New Name")
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()

    rule.onNodeWithText(getString(R.string.dialog_title_unsaved_changes)).assertExists()
    rule.onNodeWithText(getString(R.string.btn_yes)).performClick()

    rule.onNodeWithContentDescription(getString(R.string.cd_edit_routine)).assertExists()
  }
}