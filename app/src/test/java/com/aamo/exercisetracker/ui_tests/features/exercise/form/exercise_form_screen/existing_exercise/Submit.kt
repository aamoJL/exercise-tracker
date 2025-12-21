package com.aamo.exercisetracker.ui_tests.features.exercise.form.exercise_form_screen.existing_exercise

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.test_utility.ui.extensions.onNodeWithEditableText
import com.aamo.exercisetracker.test_utility.ui.extensions.waitForDisplayed
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
@Config(qualifiers = "w1000dp-h1000dp-480dpi")
@RunWith(RobolectricTestRunner::class)
class Submit : PageTest() {
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
  fun submit() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_save_exercise)).assertIsEnabled()

    val newName = "New Name"
    rule.onNodeWithText(getString(R.string.label_name)).performTextReplacement(newName)
    rule.onNodeWithText(getString(R.string.label_minutes)).performTextReplacement("10")

    rule.onNodeWithContentDescription(getString(R.string.cd_add_new_item)).assertExists()
      .performClick()

    rule.onNodeWithEditableText(set.value.toString()).performTextReplacement("1234")
    rule.onNodeWithEditableText("0").performTextReplacement("999")

    rule.onNodeWithContentDescription(getString(R.string.cd_save_exercise)).assertIsEnabled()
      .performClick()
    rule.onNodeWithContentDescription(getString(R.string.cd_edit_exercise)).waitForDisplayed()
      .assertExists()
    rule.onNodeWithText(newName).assertExists()
  }
}