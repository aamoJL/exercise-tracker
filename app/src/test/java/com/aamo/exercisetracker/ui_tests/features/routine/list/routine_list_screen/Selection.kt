package com.aamo.exercisetracker.ui_tests.features.routine.list.routine_list_screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.test_utility.ui.extensions.waitForNotDisplayed
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Selection : PageTest() {
  lateinit var routine: Routine

  @Before
  override fun setup() = runTest {
    super.setup()
    routine = toRoutineListScreen(Routine(name = "Routine 1"))
    waitForLoading()
  }

  @Test
  fun `switch selection`() {
    rule.onNodeWithText(getString(R.string.ph_search)).assertExists()
    rule.onNodeWithText(getString(R.string.x_count_selected, 1)).assertDoesNotExist()

    rule.onNodeWithText(routine.name).performTouchInput { longClick() }

    rule.onNodeWithText(getString(R.string.ph_search)).assertDoesNotExist()
    rule.onNodeWithText(getString(R.string.x_count_selected, 1)).assertExists()

    rule.onNodeWithText(routine.name).performClick()

    rule.onNodeWithText(getString(R.string.ph_search)).assertExists()
    rule.onNodeWithText(getString(R.string.x_count_selected, 1)).assertDoesNotExist()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun `deselected when deleted`() = runTest {
    rule.onNodeWithText(getString(R.string.ph_search)).assertExists()

    rule.onNodeWithText(routine.name).performTouchInput { longClick() }
    rule.onNodeWithText(getString(R.string.ph_search)).assertDoesNotExist()
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_routine)).performClick()

    rule.onNodeWithText(getString(R.string.btn_delete)).performClick() // dialog
    rule.onNodeWithText(routine.name).waitForNotDisplayed()

    rule.onNodeWithText(getString(R.string.ph_search)).assertExists()
  }
}