package com.aamo.exercisetracker.tests.features.progress_tracking.records.tracked_progress_record_screen_viewmodel

import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.features.progress_tracking.records.TrackedProgressRecordListScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.records.models.TrackedProgressRecordListModel
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Date

@Suppress("HardCodedStringLiteral")
@OptIn(ExperimentalCoroutinesApi::class)
class SaveRecord : UnconfinedTest() {
  @Test
  fun `saveRecordData called`() = runTest(UnconfinedTestDispatcher()) {
    var called: TrackedProgressValue? = null
    val model = TrackedProgressRecordListModel(
      progressName = "Progress 1",
      valueUnit = "Unit",
      records = emptyList(),
      valueType = TrackedProgressRecordListModel.ValueType.COUNT
    )
    val viewmodel = TrackedProgressRecordListScreenViewModel(
      fetchData = { flow { emit(model) } },
      deleteRecordData = { fail() },
      saveRecordData = { called = it })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    val expected = TrackedProgressValue(progressId = 1L, value = 3, addedDate = Date(2))
    viewmodel.saveRecord(expected)

    assertEquals(expected, called)
  }
}