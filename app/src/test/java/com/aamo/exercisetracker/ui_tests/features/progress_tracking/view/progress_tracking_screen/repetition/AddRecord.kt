package com.aamo.exercisetracker.ui_tests.features.progress_tracking.view.progress_tracking_screen.repetition

import androidx.compose.ui.test.isEditable
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class AddRecord : PageTest() {
  lateinit var progress: TrackedProgress

  @Before
  override fun setup() = runTest {
    super.setup()
    progress = toProgressTrackingScreen(progress = TrackedProgress(name = "Progress 1"))
    waitForLoading()
  }

  @Test
  fun `record dialog submit`() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_add)).assertExists().performClick()

    rule.onNodeWithText(getString(R.string.dialog_title_add_new_record)).assertExists()

    val value = 10
    rule.onNode(isEditable()).performTextReplacement(value.toString())
    rule.onNodeWithText(getString(R.string.btn_save)).performClick()

    rule.onNodeWithText(getString(R.string.dialog_title_add_new_record)).waitForNotDisplayed()
      .assertDoesNotExist()

    val result = trackedProgressDao.getProgressWithValuesFlow(progress.id).first()?.values
    assertEquals(listOf(value), result?.map { it.value })
  }

  @Test
  fun `record dialog cancel`() = runTest {
    rule.onNodeWithContentDescription(getString(R.string.cd_add)).assertExists().performClick()

    rule.onNodeWithText(getString(R.string.dialog_title_add_new_record)).assertExists()
    rule.onNodeWithText(getString(R.string.btn_cancel)).performClick()

    val result = trackedProgressDao.getProgressWithValuesFlow(progress.id).first()?.values
    checkNotNull(result)
    assertTrue(result.isEmpty())
  }
}