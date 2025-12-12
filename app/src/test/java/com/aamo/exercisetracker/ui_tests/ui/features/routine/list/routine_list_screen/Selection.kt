package com.aamo.exercisetracker.ui_tests.ui.features.routine.list.routine_list_screen

import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.Routine
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
  fun `select item`() {
    rule.onNodeWithText(getString(R.string.x_count_selected, 1)).assertDoesNotExist()
    rule.onNodeWithText(routine.name).performTouchInput { longClick() }
    rule.onNodeWithText(getString(R.string.x_count_selected, 1)).assertExists()
  }
}