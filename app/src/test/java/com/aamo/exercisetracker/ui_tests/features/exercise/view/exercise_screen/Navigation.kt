package com.aamo.exercisetracker.ui_tests.features.exercise.view.exercise_screen

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Navigation : PageTest() {
  lateinit var exercise: Exercise

  @Before
  override fun setup() = runTest {
    super.setup()
    exercise = toExerciseScreen(
      exercise = Exercise(routineId = 0L, name = "Exercise 1"), sets = emptyList()
    ).exercise
    waitForLoading()
  }

  @Test
  fun `on exerciseScreen`() = runTest {
    rule.onNodeWithText(exercise.name).assertExists()
    rule.onNodeWithContentDescription(getString(R.string.cd_edit_exercise)).assertExists()
  }

  @Test
  fun back() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()
    rule.onNodeWithContentDescription(getString(R.string.cd_add_exercise)).assertExists()
  }

  @Test
  fun `to exerciseFormScreen`() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_edit_exercise)).assertExists()
      .performClick()
    waitForLoading()

    rule.onNodeWithText(getString(R.string.title_existing_exercise)).assertExists()
  }
}