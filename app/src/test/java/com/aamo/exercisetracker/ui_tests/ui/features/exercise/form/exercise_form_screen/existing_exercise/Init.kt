package com.aamo.exercisetracker.ui_tests.ui.features.exercise.form.exercise_form_screen.existing_exercise

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.test_utility.ui.extensions.assertEditableText
import com.aamo.exercisetracker.test_utility.ui.extensions.onNodeWithEditableText
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
class Init : PageTest() {
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
  fun `delete button visible`() {
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_exercise)).assertExists()
  }

  @Test
  fun `form fields`() {
    rule.onNodeWithText(getString(R.string.label_name)).assertEditableText(exercise.name)
    rule.onNodeWithText(getString(R.string.label_rest_duration_optional))
      .assertEditableText(exercise.restDuration.inWholeMinutes.toInt().toString())
    rule.onNodeWithText(getString(R.string.label_set_unit)).assertEditableText(set.unit)
    rule.onNodeWithText(getString(R.string.label_timer)).assertExists().assertIsOff()
    rule.onNodeWithEditableText(set.value.toString()).assertExists()
  }
}