package com.aamo.exercisetracker.ui_tests.ui.features.progress_tracking.view.progress_tracking_screen.countdown

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import com.aamo.exercisetracker.utility.extensions.date.toClockString
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Suppress("HardCodedStringLiteral")
@Config(qualifiers = "w1000dp-h1000dp-480dpi")
@RunWith(RobolectricTestRunner::class)
class OpenStopwatch : PageTest() {
  lateinit var progress: TrackedProgress

  @Before
  override fun setup() = runTest {
    super.setup()
    progress = toProgressTrackingScreen(
      progress = TrackedProgress(
        name = "Progress 1", timerTime = 5.seconds.inWholeMilliseconds
      )
    )
    waitForLoading()
  }

  @Test
  fun open() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_timer)).assertExists().performClick()
    rule.onNodeWithText(getString(R.string.title_timer)).assertExists()
    rule.onNodeWithText(progress.timerTime!!.milliseconds.toClockString()).assertExists()
    rule.onNodeWithContentDescription(getString(R.string.btn_start)).assertExists()
    rule.onNodeWithContentDescription(getString(R.string.btn_cancel)).assertExists()
  }
}