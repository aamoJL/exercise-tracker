package com.aamo.exercisetracker.ui_tests.ui.features.progress_tracking.form.tracked_progress_form_screen.existing_tracked_progress

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Submit : PageTest() {
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
        timerTime = 3.minutes.inWholeMilliseconds
      )
    )
    waitForLoading()
  }

  @Test
  fun submit() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_save_tracked_progress))
      .assertIsEnabled()

    rule.onNodeWithText(getString(R.string.label_name)).performTextReplacement("New name")

    rule.onNodeWithContentDescription(getString(R.string.cd_save_tracked_progress))
      .assertIsEnabled().performClick()
    waitForLoading()

    rule.onNodeWithContentDescription(getString(R.string.cd_edit_tracked_progress)).assertExists()
  }
}