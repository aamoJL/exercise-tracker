package com.aamo.exercisetracker.ui_tests.ui.features.progress_tracking.view.progress_tracking_screen.stopwatch

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
import org.robolectric.annotation.Config

@Suppress("HardCodedStringLiteral")
@Config(qualifiers = "w1000dp-h1000dp-480dpi")
@RunWith(RobolectricTestRunner::class)
class OpenStopwatch : PageTest() {
  lateinit var progress: TrackedProgress

  @Before
  override fun setup() = runTest {
    super.setup()
    progress =
      toProgressTrackingScreen(progress = TrackedProgress(name = "Progress 1", hasStopWatch = true))
    waitForLoading()
  }

  @Test
  fun open() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_timer)).assertExists().performClick()
    rule.onNodeWithText(getString(R.string.title_stopwatch)).assertExists()
    rule.onNodeWithContentDescription(getString(R.string.btn_start)).assertExists()
    rule.onNodeWithContentDescription(getString(R.string.btn_cancel)).assertExists()
  }
}