package com.aamo.exercisetracker.ui_tests.features.routine.list.routine_list_screen

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextReplacement
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
class Search : PageTest() {
  lateinit var routine: Routine

  @Before
  override fun setup() = runTest {
    super.setup()
    routine = toRoutineListScreen(Routine(name = "Routine 1"))
    waitForLoading()
  }

  @Test
  fun filter() = runTest {
    rule.onNodeWithText(routine.name).assertExists()

    val filter = "asd"
    rule.onNodeWithText(getString(R.string.ph_search)).performTextReplacement(filter)
    rule.onNodeWithText(routine.name).assertDoesNotExist()

    rule.onNodeWithText(filter).performTextReplacement(routine.name.substring(0, 2))
    rule.onNodeWithText(routine.name).assertExists()
  }
}