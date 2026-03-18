package com.aamo.exercisetracker.ui_tests.features.exercise.view.exercise_screen.repetitions.without_rest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.test_utility.ui.extensions.waitForDisplayed
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Suppress("HardCodedStringLiteral")
@Config(qualifiers = "w1000dp-h1000dp-480dpi")
@RunWith(RobolectricTestRunner::class)
class SetProgress : PageTest() {
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
  fun `set progress indicator`() = runTest {
    rule.onNodeWithText(getString(R.string.title_completed)).assertDoesNotExist()
    rule.onNodeWithText(getString(R.string.title_set)).assertExists()

    sets.forEachIndexed { i, _ ->
      rule.onNodeWithText("${i + 1}/${sets.size}").assertExists()
      rule.onNodeWithText(getString(R.string.btn_done)).assertIsDisplayed().performClick()
    }

    rule.onNodeWithText(getString(R.string.title_completed)).assertExists()
  }

  @Test
  fun `current set info`() = runTest {
    sets.forEachIndexed { _, set ->
      rule.onNodeWithText(getString(R.string.title_current_set)).assertExists()
      rule.onNodeWithText("${set.value} ${set.unit}").assertExists()
      rule.onNodeWithText(getString(R.string.btn_done)).assertIsDisplayed().performClick()
    }

    rule.onNodeWithText(getString(R.string.title_current_set)).assertDoesNotExist()
  }

  @Test
  fun `finish exercise`() = runTest {
    sets.forEachIndexed { _, _ ->
      rule.onNodeWithText(getString(R.string.btn_done)).performClick()
    }

    rule.onNodeWithText(getString(R.string.title_completed)).assertExists()
    rule.onNodeWithText(getString(R.string.btn_complete)).performClick()
    waitForLoading()
    rule.onNodeWithContentDescription(getString(R.string.cd_edit_routine)).waitForDisplayed()
      .assertExists()
  }
}