package com.aamo.exercisetracker.ui_tests.features.routine.form.routine_form_screen.existing_routine

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Delete : PageTest() {
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
  fun `delete dialog cancel`() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_routine)).assertIsEnabled()
      .performClick()

    rule.onNodeWithText(getString(R.string.btn_cancel)).assertExists().performClick()

    rule.onNodeWithText(getString(R.string.title_existing_routine)).assertExists()
  }

  @Test
  fun `delete dialog confirm`() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_routine)).assertIsEnabled()
      .performClick()

    rule.onNodeWithText(getString(R.string.btn_delete)).assertExists().performClick()

    rule.onNodeWithText(getString(R.string.ph_search)).assertExists()
    rule.onNodeWithText(model.routine.name).assertDoesNotExist()
  }
}