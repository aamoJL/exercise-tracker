package com.aamo.exercisetracker.ui_tests.ui.features.progress_tracking.list.tracked_progress_list_screen

import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Selection : PageTest() {
  lateinit var progress: TrackedProgress

  @Before
  override fun setup() = runTest {
    super.setup()
    progress = toTrackedProgressListScreen(TrackedProgress(name = "Progress 1"))
    waitForLoading()
  }

  @Test
  fun `switch selection`() {
    rule.onNodeWithText(getString(R.string.x_count_selected, 1)).assertDoesNotExist()
    rule.onNodeWithText(progress.name).performTouchInput { longClick() }
    rule.onNodeWithText(getString(R.string.x_count_selected, 1)).assertExists()
    rule.onNodeWithText(progress.name).performClick()
    rule.onNodeWithText(getString(R.string.x_count_selected, 1)).assertDoesNotExist()
  }
}