package com.aamo.exercisetracker.ui_tests.features.exercise.form.exercise_form_screen.existing_exercise

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.test_utility.ui.extensions.waitForNotDisplayed
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Navigation : PageTest() {
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
  fun `on exerciseFormScreen`() = runTest {
    rule.onNodeWithText(getString(R.string.title_existing_exercise)).assertExists()
  }

  @Test
  fun back() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()
    rule.onNodeWithText(getString(R.string.title_set)).assertExists()
    rule.onNodeWithText(exercise.name).assertExists()
  }

  @Test
  fun `unsavedChanges dialog cancel`() = runTest {
    rule.onNodeWithText(getString(R.string.label_name)).performTextReplacement("Name")
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()

    rule.onNodeWithText(getString(R.string.dialog_title_unsaved_changes)).assertExists()
    rule.onNodeWithText(getString(R.string.btn_cancel)).performClick()

    rule.onNodeWithText(getString(R.string.title_existing_exercise)).assertExists()
  }

  @Test
  fun `unsavedChanges dialog confirm`() = runTest {
    val newName = "New Name"
    rule.onNodeWithText(getString(R.string.label_name)).performTextReplacement(newName)
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()

    rule.onNodeWithText(getString(R.string.dialog_title_unsaved_changes)).assertExists()
    rule.onNodeWithText(getString(R.string.btn_yes)).performClick()
    rule.onNodeWithText(getString(R.string.dialog_title_unsaved_changes)).waitForNotDisplayed()

    rule.onNodeWithContentDescription(getString(R.string.cd_edit_exercise)).assertExists()
    rule.onNodeWithText(exercise.name).assertExists()
    rule.onNodeWithText(newName).assertDoesNotExist()
  }
}