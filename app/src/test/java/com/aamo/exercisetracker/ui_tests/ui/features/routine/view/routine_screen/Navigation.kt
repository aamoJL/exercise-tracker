package com.aamo.exercisetracker.ui_tests.ui.features.routine.view.routine_screen

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.test_utility.ui.extensions.waitForDisplayed
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Navigation : PageTest() {
  lateinit var routine: Routine

  @Before
  override fun setup() = runTest {
    super.setup()
    routine = toRoutineScreen(Routine(name = "Routine 1"))
    waitForLoading()
  }

  @Test
  fun `on routineScreen`() = runTest {
    rule.onNodeWithText(routine.name).assertExists()
    rule.onNodeWithContentDescription(getString(R.string.cd_edit_routine)).assertExists()
  }

  @Test
  fun back() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()
    rule.onNodeWithContentDescription(getString(R.string.cd_add_routine)).assertExists()
  }

  @Test
  fun `to routineFormScreen`() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_edit_routine)).assertExists()
      .performClick()
    waitForLoading()

    rule.onNodeWithText(getString(R.string.title_existing_routine)).assertExists()
  }

  @Test
  fun `to exerciseFormScreen`() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_add_exercise)).assertExists()
      .performClick()
    waitForLoading()

    rule.onNodeWithText(getString(R.string.title_new_exercise)).assertExists()
  }

  @Test
  fun `to exerciseScreen`() = runTest {
    val exercise = Exercise(routineId = routine.id, name = "Exercise 1").also {
      routineDao.upsert(it)
    }

    rule.onNodeWithText(exercise.name).waitForDisplayed().performClick()

    rule.onNodeWithContentDescription(getString(R.string.cd_edit_exercise)).assertExists()
  }
}