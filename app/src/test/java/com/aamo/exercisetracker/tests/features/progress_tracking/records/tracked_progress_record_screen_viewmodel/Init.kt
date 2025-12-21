package com.aamo.exercisetracker.tests.features.progress_tracking.records.tracked_progress_record_screen_viewmodel

import com.aamo.exercisetracker.features.progress_tracking.records.TrackedProgressRecordListScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.records.models.TrackedProgressRecordListModel
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@Suppress("HardCodedStringLiteral")
@OptIn(ExperimentalCoroutinesApi::class)
class Init : UnconfinedTest() {
  @Test
  fun `model set`() = runTest(UnconfinedTestDispatcher()) {
    val dataFlow = MutableSharedFlow<TrackedProgressRecordListModel>()
    val viewmodel = TrackedProgressRecordListScreenViewModel(
      fetchData = { dataFlow },
      deleteRecordData = {},
      saveRecordData = {})

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    assertNull(viewmodel.model.value)

    val model = TrackedProgressRecordListModel(
      progressName = "Progress 1",
      valueUnit = "Unit",
      records = emptyList(),
      valueType = TrackedProgressRecordListModel.ValueType.COUNT
    )

    dataFlow.emit(model)
    assertEquals(model, viewmodel.model.value)
  }
}