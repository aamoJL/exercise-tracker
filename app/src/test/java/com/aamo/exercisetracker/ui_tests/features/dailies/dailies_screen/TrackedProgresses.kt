package com.aamo.exercisetracker.ui_tests.features.dailies.dailies_screen

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
class TrackedProgresses : PageTest() {
  @Before
  override fun setup() = runTest {
    super.setup()
    waitForLoading()
  }

  @Test
  fun `unfinished tracked progresses visible`() = runTest {
    val unfinishedProgress = TrackedProgress(name = "Unfinished", intervalWeeks = 1).also {
      trackedProgressDao.upsert(it)
    }
    val finishedProgress = TrackedProgress(name = "Finished", intervalWeeks = 0).also {
      trackedProgressDao.upsert(it)
    }

    rule.onNodeWithContentDescription(getString(R.string.cd_show_unfinished_tracked_progresses))
      .waitForDisplayed().performClick()
    rule.onNodeWithText(unfinishedProgress.name).waitForDisplayed().assertExists()
    rule.onNodeWithText(finishedProgress.name).assertDoesNotExist()
  }
}