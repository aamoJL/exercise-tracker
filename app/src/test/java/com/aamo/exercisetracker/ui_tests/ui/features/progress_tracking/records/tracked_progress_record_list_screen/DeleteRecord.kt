package com.aamo.exercisetracker.ui_tests.ui.features.progress_tracking.records.tracked_progress_record_list_screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.test_utility.ui.extensions.waitForDisplayed
import com.aamo.exercisetracker.test_utility.ui.extensions.waitForNotDisplayed
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.text.SimpleDateFormat
import java.util.Date

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class DeleteRecord : PageTest() {
  lateinit var record: TrackedProgressValue

  @Before
  override fun setup() = runTest {
    super.setup()
    toTrackedProgressRecordListScreen(
      progress = TrackedProgress(name = "Progress 1"),
      record = TrackedProgressValue(progressId = 0L, value = 123, addedDate = Date(1))
    ).also {
      record = it.values.first()
    }
    waitForLoading()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun `delete dialog confirm`() = runTest {
    rule.onNodeWithText(SimpleDateFormat.getDateInstance().format(record.addedDate)).assertExists()
      .performClick()
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_record)).performClick()

    // dialog
    rule.onNodeWithText(getString(R.string.dialog_title_delete_record)).waitForDisplayed()
    rule.onNodeWithText(getString(R.string.btn_delete)).performClick()
    rule.onNodeWithText(getString(R.string.dialog_title_delete_record)).waitForNotDisplayed()

    rule.onNodeWithText(SimpleDateFormat.getDateInstance().format(record.addedDate))
      .waitForNotDisplayed().assertDoesNotExist()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun `delete dialog cancel`() = runTest {
    rule.onNodeWithText(SimpleDateFormat.getDateInstance().format(record.addedDate)).assertExists()
      .performClick()
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_record)).performClick()

    // dialog
    rule.onNodeWithText(getString(R.string.dialog_title_delete_record)).waitForDisplayed()
    rule.onNodeWithText(getString(R.string.btn_cancel)).performClick()
    rule.onNodeWithText(getString(R.string.dialog_title_delete_record)).waitForNotDisplayed()

    rule.onNodeWithText(SimpleDateFormat.getDateInstance().format(record.addedDate))
      .waitForDisplayed().assertExists()
  }
}