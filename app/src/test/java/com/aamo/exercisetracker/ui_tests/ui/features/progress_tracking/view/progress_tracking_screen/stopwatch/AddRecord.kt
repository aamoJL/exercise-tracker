package com.aamo.exercisetracker.ui_tests.ui.features.progress_tracking.view.progress_tracking_screen.stopwatch

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.test_utility.ui.extensions.waitForNotDisplayed
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Suppress("HardCodedStringLiteral")
@Config(qualifiers = "w1000dp-h1000dp-480dpi")
@RunWith(RobolectricTestRunner::class)
class AddRecord : PageTest() {
  lateinit var progress: TrackedProgress

  @Before
  override fun setup() = runTest {
    super.setup()
    progress =
      toProgressTrackingScreen(progress = TrackedProgress(name = "Progress 1", hasStopWatch = true))
    waitForLoading()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `record dialog submit`() = runTest(UnconfinedTestDispatcher()) {
    rule.onNodeWithContentDescription(getString(R.string.cd_add)).assertExists().performClick()

    rule.onNodeWithText(getString(R.string.dialog_title_add_new_record)).assertExists()

    rule.onNodeWithText(getString(R.string.label_hours)).assertExists().performTextReplacement("1")
    rule.onNodeWithText(getString(R.string.label_minutes)).assertExists()
      .performTextReplacement("1")
    rule.onNodeWithText(getString(R.string.label_seconds)).assertExists()
      .performTextReplacement("1")
    rule.onNodeWithText(getString(R.string.btn_save)).performClick()

    rule.onNodeWithText(getString(R.string.dialog_title_add_new_record)).waitForNotDisplayed()
      .assertDoesNotExist()

    val result = trackedProgressDao.getProgressValuesFlow(progress.id).first()
    assertEquals(
      listOf((1.hours + 1.minutes + 1.seconds).inWholeMilliseconds.toInt()),
      result.map { it.value })
  }

  @Test
  fun `record dialog cancel`() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_add)).assertExists().performClick()

    rule.onNodeWithText(getString(R.string.dialog_title_add_new_record)).assertExists()
    rule.onNodeWithText(getString(R.string.btn_cancel)).performClick()

    val result = trackedProgressDao.getProgressValuesFlow(progress.id).first()
    assertTrue(result.isEmpty())
  }
}