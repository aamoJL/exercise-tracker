package com.aamo.exercisetracker.ui_tests.ui.features.progress_tracking.records.tracked_progress_record_list_screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
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
class EditRecord : PageTest() {
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
  fun `edit dialog confirm`() = runTest {
    rule.onNodeWithText(SimpleDateFormat.getDateInstance().format(record.addedDate)).assertExists()
      .performClick()
    rule.onNodeWithContentDescription(getString(R.string.cd_edit_record)).performClick()

    // dialog
    val newValue = 999
    rule.onNodeWithText(getString(R.string.dialog_title_edit_record)).waitForDisplayed()
    rule.onNodeWithText(record.value.toString()).assertExists()
      .performTextReplacement(newValue.toString())
    rule.onNodeWithText(getString(R.string.btn_save)).performClick()
    rule.onNodeWithText(getString(R.string.dialog_title_edit_record)).waitForNotDisplayed()

    rule.onNodeWithText(SimpleDateFormat.getDateInstance().format(record.addedDate)).assertExists()
    rule.onNodeWithText(record.value.toString(), substring = true).waitForNotDisplayed()
      .assertDoesNotExist()
    rule.onNodeWithText(newValue.toString(), substring = true).assertExists()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun `edit dialog cancel`() = runTest {
    rule.onNodeWithText(SimpleDateFormat.getDateInstance().format(record.addedDate)).assertExists()
      .performClick()
    rule.onNodeWithContentDescription(getString(R.string.cd_edit_record)).performClick()

    // dialog
    val newValue = 999
    rule.onNodeWithText(getString(R.string.dialog_title_edit_record)).waitForDisplayed()
    rule.onNodeWithText(record.value.toString()).assertExists()
      .performTextReplacement(newValue.toString())
    rule.onNodeWithText(getString(R.string.btn_cancel)).performClick()
    rule.onNodeWithText(getString(R.string.dialog_title_edit_record)).waitForNotDisplayed()

    rule.onNodeWithText(SimpleDateFormat.getDateInstance().format(record.addedDate)).assertExists()
    rule.onNodeWithText(record.value.toString(), substring = true).assertExists()
    rule.onNodeWithText(newValue.toString(), substring = true).assertDoesNotExist()
  }
}