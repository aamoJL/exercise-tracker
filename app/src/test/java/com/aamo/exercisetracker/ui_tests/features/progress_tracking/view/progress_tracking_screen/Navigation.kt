package com.aamo.exercisetracker.ui_tests.features.progress_tracking.view.progress_tracking_screen

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
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
    progress = toProgressTrackingScreen(progress = TrackedProgress(name = "Progress 1"))
    waitForLoading()
  }

  @Test
  fun `on progressTrackingScreen`() = runTest {
    rule.onNodeWithText(progress.name).assertExists()
    rule.onNodeWithContentDescription(getString(R.string.cd_edit_tracked_progress)).assertExists()
  }

  @Test
  fun back() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_navigate_back)).performClick()
    rule.onNodeWithText(getString(R.string.ph_search)).assertExists()
  }

  @Test
  fun `to trackedProgressFormScreen`() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_edit_tracked_progress)).performClick()
    waitForLoading()

    rule.onNodeWithText(getString(R.string.title_existing_tracked_progress)).assertExists()
  }

  @Test
  fun `to trackedProgressRecordListScreen`() = runTest {
    TrackedProgressValue(progressId = progress.id, addedDate = Date(1)).let {
      trackedProgressDao.upsert(it).let { id -> it.copy(id = id) }
    }

    rule.onNodeWithContentDescription(getString(R.string.cd_show_records)).performClick()
    waitForLoading()

    rule.onNodeWithText(getString(R.string.title_tracked_progress_records, progress.name))
  }
}