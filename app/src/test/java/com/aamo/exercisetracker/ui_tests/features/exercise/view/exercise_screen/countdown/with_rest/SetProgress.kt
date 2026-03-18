package com.aamo.exercisetracker.ui_tests.features.exercise.view.exercise_screen.countdown.with_rest

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
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

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
      exercise = Exercise(routineId = 0L, restDuration = 3.minutes), sets = listOf(
        ExerciseSet(
          exerciseId = 0L,
          value = 1.minutes.inWholeMilliseconds.toInt(),
          unit = "Reps",
          valueType = ExerciseSet.ValueType.COUNTDOWN
        ),
        ExerciseSet(
          exerciseId = 0L,
          value = 2.minutes.inWholeMilliseconds.toInt(),
          unit = "Reps",
          valueType = ExerciseSet.ValueType.COUNTDOWN
        ),
        ExerciseSet(
          exerciseId = 0L,
          value = 3.minutes.inWholeMilliseconds.toInt(),
          unit = "Reps",
          valueType = ExerciseSet.ValueType.COUNTDOWN
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

    sets.forEachIndexed { i, set ->
      rule.onNodeWithText("${i + 1}/${sets.size}").assertExists()
      rule.onNodeWithText(getString(R.string.btn_start)).assertIsDisplayed().performClick()

      rule.onNodeWithText(getString(R.string.title_set_timer)).assertExists()
      rule.onNodeWithContentDescription(getString(R.string.btn_stop)).assertExists().performClick()

      if (set != sets.last()) {
        rule.onNodeWithText(getString(R.string.title_rest)).assertExists()
        rule.onNodeWithContentDescription(getString(R.string.btn_stop)).assertExists()
          .performClick()
      }
      else {
        rule.onNodeWithText(getString(R.string.title_rest)).assertDoesNotExist()
      }
    }

    rule.onNodeWithText(getString(R.string.title_completed)).assertExists()
  }

  @Test
  fun `current set info`() = runTest {
    sets.forEachIndexed { _, set ->
      rule.onNodeWithText(getString(R.string.title_current_set)).assertExists()
      rule.onNodeWithText(
        getString(R.string.x_minutes_short, set.value.milliseconds.inWholeMinutes.toInt())
      ).assertExists()
      rule.onNodeWithText(getString(R.string.btn_start)).assertIsDisplayed().performClick()

      rule.onNodeWithText(getString(R.string.title_set_timer)).assertExists()
      rule.onNodeWithContentDescription(getString(R.string.btn_stop)).assertExists().performClick()

      if (set != sets.last()) {
        rule.onNodeWithText(getString(R.string.title_rest)).assertExists()
        rule.onNodeWithContentDescription(getString(R.string.btn_stop)).assertExists()
          .performClick()
      }
      else {
        rule.onNodeWithText(getString(R.string.title_rest)).assertDoesNotExist()
      }
    }

    rule.onNodeWithText(getString(R.string.title_current_set)).assertDoesNotExist()
  }

  @Test
  fun `finish exercise`() = runTest {
    sets.forEachIndexed { _, set ->
      rule.onNodeWithText(getString(R.string.btn_start)).performClick()

      rule.onNodeWithText(getString(R.string.title_set_timer)).assertExists()
      rule.onNodeWithContentDescription(getString(R.string.btn_stop)).assertExists().performClick()

      if (set != sets.last()) {
        rule.onNodeWithText(getString(R.string.title_rest)).assertExists()
        rule.onNodeWithContentDescription(getString(R.string.btn_stop)).assertExists()
          .performClick()
      }
      else {
        rule.onNodeWithText(getString(R.string.title_rest)).assertDoesNotExist()
      }
    }

    rule.onNodeWithText(getString(R.string.title_completed)).assertExists()
    rule.onNodeWithText(getString(R.string.btn_complete)).performClick()
    waitForLoading()
    rule.onNodeWithContentDescription(getString(R.string.cd_edit_routine)).waitForDisplayed()
      .assertExists()
  }
}