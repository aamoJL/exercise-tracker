package com.aamo.exercisetracker.ui_tests.ui.features.progress_tracking.list.tracked_progress_list_screen

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.test_utility.ui.extensions.waitForDisplayed
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Navigation : PageTest() {
  lateinit var progress: TrackedProgress

  @Before
  override fun setup() = runTest {
    super.setup()
    progress = toTrackedProgressListScreen(TrackedProgress(name = "Progress 1"))
    waitForLoading()
  }

  @Test
  fun `on trackedProgressListScreen`() = runTest {
    rule.onNodeWithText(progress.name).assertExists()
  }

  @Test
  fun `to progressTrackingPage`() = runTest {
    rule.onNodeWithText(progress.name).performClick()
    waitForLoading()

    rule.onNodeWithContentDescription(getString(R.string.cd_show_records)).waitForDisplayed()
      .assertExists()
  }

  @Test
  fun `to trackedProgressFormScreen`() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_add_tracked_progress)).performClick()
    waitForLoading()

    rule.onNodeWithText(getString(R.string.title_new_tracked_progress)).assertExists()
  }
}