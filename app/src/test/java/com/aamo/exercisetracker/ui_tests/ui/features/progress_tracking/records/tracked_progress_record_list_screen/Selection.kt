package com.aamo.exercisetracker.ui_tests.ui.features.progress_tracking.records.tracked_progress_record_list_screen

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
import java.text.SimpleDateFormat
import java.util.Date

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Selection : PageTest() {
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

  @Test
  fun `switch selection`() = runTest {
    rule.onNodeWithText(SimpleDateFormat.getDateInstance().format(record.addedDate)).assertExists()
      .performClick()
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_record)).assertExists()
    rule.onNodeWithContentDescription(getString(R.string.cd_edit_record)).assertExists()

    rule.onNodeWithText(SimpleDateFormat.getDateInstance().format(record.addedDate)).performClick()
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_record)).assertDoesNotExist()
    rule.onNodeWithContentDescription(getString(R.string.cd_edit_record)).assertDoesNotExist()
  }
}