package com.aamo.exercisetracker.ui_tests.features.routine.list.routine_list_screen

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
    routine = toRoutineListScreen(Routine(name = "Routine 1"))
    waitForLoading()
  }

  @Test
  fun `on routineListScreen`() = runTest {
    rule.onNodeWithText(routine.name).assertExists()
  }

  @Test
  fun `to routinePage`() = runTest {
    rule.onNodeWithText(routine.name).performClick()
    waitForLoading()

    val exercise = Exercise(routineId = routine.id, name = "Exercise 1").also {
      routineDao.upsert(it)
    }

    rule.onNodeWithText(exercise.name).waitForDisplayed().assertExists()
  }

  @Test
  fun `to routineFormScreen`() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_add_routine)).performClick()
    waitForLoading()

    rule.onNodeWithText(getString(R.string.title_new_routine)).assertExists()
  }
}