package com.aamo.exercisetracker.ui_tests.features.progress_tracking.form.tracked_progress_form_screen.existing_tracked_progress

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class Delete : PageTest() {
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
  fun `delete dialog cancel`() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_tracked_progress))
      .assertIsEnabled().performClick()

    rule.onNodeWithText(getString(R.string.btn_cancel)).assertExists().performClick()

    rule.onNodeWithText(getString(R.string.title_existing_tracked_progress)).assertExists()
  }

  @Test
  fun `delete dialog confirm`() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_tracked_progress))
      .assertIsEnabled().performClick()

    rule.onNodeWithText(getString(R.string.btn_delete)).assertExists().performClick()

    rule.onNodeWithText(getString(R.string.ph_search)).assertExists()
    rule.onNodeWithText(model.name).assertDoesNotExist()
  }
}