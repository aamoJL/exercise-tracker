package com.aamo.exercisetracker.ui_tests.features.progress_tracking.form.tracked_progress_form_screen.existing_tracked_progress

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.test_utility.ui.extensions.assertEditableText
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Suppress("HardCodedStringLiteral")
@Config(qualifiers = "w1000dp-h1000dp-480dpi")
@RunWith(RobolectricTestRunner::class)
class Init : PageTest() {
  lateinit var model: TrackedProgress

  @Before
  override fun setup() = runTest {
    super.setup()
    model = toTrackedProgressFormScreen(
      model = TrackedProgress(
        id = 1L,
        name = "Progress 1",
        intervalWeeks = 2,
        unit = "Unit",
        hasStopWatch = false,
        timerTime = 3.minutes.inWholeMilliseconds + 25.seconds.inWholeMilliseconds
      )
    )
    waitForLoading()
  }

  @Test
  fun `delete button visible`() {
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_tracked_progress)).assertExists()
      .assertIsEnabled()
  }

  @Test
  fun `form fields`() {
    rule.onNodeWithText(getString(R.string.label_name)).assertEditableText(model.name)
    rule.onNodeWithText(getString(R.string.label_weekly_interval_optional))
      .assertEditableText(model.intervalWeeks.toString())
    rule.onNodeWithText(getString(R.string.label_progress_unit)).assertEditableText(model.unit)
    rule.onNodeWithText(getString(R.string.label_minutes)).assertEditableText("03")
      .assertIsEnabled()
    rule.onNodeWithText(getString(R.string.label_seconds)).assertEditableText("25")
      .assertIsEnabled()
    rule.onNodeWithText(getString(R.string.label_repetitions)).assertIsNotSelected()
    rule.onNodeWithText(getString(R.string.label_timer)).assertIsSelected()
    rule.onNodeWithText(getString(R.string.label_stopwatch)).assertIsNotSelected()
  }
}