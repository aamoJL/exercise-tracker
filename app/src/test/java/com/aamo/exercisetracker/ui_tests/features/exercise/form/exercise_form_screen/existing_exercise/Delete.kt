package com.aamo.exercisetracker.ui_tests.features.exercise.form.exercise_form_screen.existing_exercise

import androidx.compose.ui.test.assertIsEnabled
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
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Delete : PageTest() {
  lateinit var exercise: Exercise
  lateinit var set: ExerciseSet

  @Before
  override fun setup() = runTest {
    super.setup()
    toExerciseFormScreen(
      model = Exercise(routineId = 0L, name = "Exercise 1", restDuration = 3.minutes)
    ).also {
      exercise = it.exercise
      set = it.sets.first()
    }
    waitForLoading()
  }

  @Test
  fun `delete dialog cancel`() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_exercise)).assertIsEnabled()
      .performClick()

    rule.onNodeWithText(getString(R.string.btn_cancel)).assertExists().performClick()

    rule.onNodeWithText(getString(R.string.title_existing_exercise)).assertExists()
  }

  @Test
  fun `delete dialog confirm`() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_exercise)).assertIsEnabled()
      .performClick()

    rule.onNodeWithText(getString(R.string.btn_delete)).assertExists().performClick()

    rule.onNodeWithContentDescription(getString(R.string.cd_edit_routine)).assertExists()
    rule.onNodeWithText(exercise.name).assertDoesNotExist()
  }
}