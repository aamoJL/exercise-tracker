package com.aamo.exercisetracker.ui_tests.ui.features.routine.list.routine_list_screen

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
class DeleteItem : PageTest() {
  lateinit var routine: Routine

  @Before
  override fun setup() = runTest {
    super.setup()
    routine = toRoutineListScreen(Routine(name = "Routine 1"))
    waitForLoading()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun `delete item`() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_routine)).assertDoesNotExist()
    rule.onNodeWithText(routine.name).performTouchInput { longClick() }
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_routine)).assertExists()
      .performClick()

    rule.onNodeWithText(getString(R.string.btn_delete)).performClick() // dialog
    rule.onNodeWithText(routine.name).waitForNotDisplayed().assertDoesNotExist()
  }
}