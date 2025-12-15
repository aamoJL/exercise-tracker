package com.aamo.exercisetracker.ui_tests.ui.features.progress_tracking.records.tracked_progress_record_list_screen

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.test_utility.ui.extensions.waitForDisplayed
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Navigation : PageTest() {
  lateinit var progress: TrackedProgress

  @Before
  override fun setup() = runTest {
    super.setup()
    toTrackedProgressRecordListScreen(
      progress = TrackedProgress(name = "Progress 1"),
      record = TrackedProgressValue(progressId = 0L, value = 123, addedDate = Date(1))
    ).also {
      progress = it.trackedProgress
    }
    waitForLoading()
  }

  @Test
  fun `on trackedProgressRecordListScreen`() = runTest {
    rule.onNodeWithText(getString(R.string.title_tracked_progress_records, progress.name))
      .assertExists()
  }

  @Test
  fun back() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()
    rule.onNodeWithContentDescription(getString(R.string.cd_show_records)).waitForDisplayed()
      .assertExists()
  }
}