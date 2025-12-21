package com.aamo.exercisetracker.ui_tests.features.exercise.view.exercise_screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class InProgress : PageTest() {
  lateinit var exercise: Exercise
  lateinit var sets: List<ExerciseSet>

  @Before
  override fun setup() = runTest {
    super.setup()
    toExerciseScreen(
      exercise = Exercise(routineId = 0L), sets = listOf(
        ExerciseSet(
          exerciseId = 0L, value = 1, unit = "Reps", valueType = ExerciseSet.ValueType.REPETITION
        ),
        ExerciseSet(
          exerciseId = 0L, value = 2, unit = "Reps", valueType = ExerciseSet.ValueType.REPETITION
        ),
        ExerciseSet(
          exerciseId = 0L, value = 3, unit = "Reps", valueType = ExerciseSet.ValueType.REPETITION
        ),
      )
    ).also {
      exercise = it.exercise
      sets = it.sets
    }
    waitForLoading()
  }

  @Test
  fun `show in progress dialog`() = runTest {
    rule.onNodeWithText(getString(R.string.btn_done)).assertIsDisplayed().performClick()
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()

    rule.onNodeWithText(getString(R.string.dialog_text_exercise_in_progress)).assertExists()
  }

  @Test
  fun `in progress dialog cancel`() = runTest {
    rule.onNodeWithText(getString(R.string.title_set)).assertExists()

    rule.onNodeWithText(getString(R.string.btn_done)).assertIsDisplayed().performClick()
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()
    rule.onNodeWithText(getString(R.string.dialog_text_exercise_in_progress)).assertExists()
    rule.onNodeWithText(getString(R.string.btn_cancel)).performClick()

    rule.onNodeWithText(getString(R.string.title_set)).assertExists()
  }

  @Test
  fun `in progress dialog confirm`() = runTest {
    rule.onNodeWithText(getString(R.string.title_set)).assertExists()

    rule.onNodeWithText(getString(R.string.btn_done)).assertIsDisplayed().performClick()
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()
    rule.onNodeWithText(getString(R.string.dialog_text_exercise_in_progress)).assertExists()
    rule.onNodeWithText(getString(R.string.btn_yes)).performClick()

    rule.onNodeWithContentDescription(getString(R.string.cd_add_exercise)).assertExists()
  }
}