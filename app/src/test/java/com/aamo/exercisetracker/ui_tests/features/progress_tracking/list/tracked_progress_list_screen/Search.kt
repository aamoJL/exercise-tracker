package com.aamo.exercisetracker.ui_tests.features.progress_tracking.list.tracked_progress_list_screen

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextReplacement
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
class Search : PageTest() {
  lateinit var progress: TrackedProgress

  @Before
  override fun setup() = runTest {
    super.setup()
    progress = toTrackedProgressListScreen(TrackedProgress(name = "Progress 1"))
    waitForLoading()
  }

  @Test
  fun filter() = runTest {
    rule.onNodeWithText(progress.name).assertExists()

    val filter = "asd"
    rule.onNodeWithText(getString(R.string.ph_search)).performTextReplacement(filter)
    rule.onNodeWithText(progress.name).assertDoesNotExist()

    rule.onNodeWithText(filter).performTextReplacement(progress.name.substring(0, 2))
    rule.onNodeWithText(progress.name).assertExists()
  }
}