package com.aamo.exercisetracker.tests.features.progress_tracking.view.progress_tracking_screen_viewmodel

import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.features.progress_tracking.view.ProgressTrackingScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.view.models.ProgressTrackingTrackedProgressModel
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import junit.framework.TestCase.assertEquals
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
class AddNewValue : UnconfinedTest() {
  @Test
  fun `addValue called`() = runTest(UnconfinedTestDispatcher()) {
    var called: TrackedProgressValue? = null
    val model = ProgressTrackingTrackedProgressModel(
      id = 1L,
      name = "Progress 1",
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.REPETITION,
      values = emptyList(),
      recordUnit = "Unit",
      countdownTime = null
    )
    val viewmodel = ProgressTrackingScreenViewModel(
      fetchData = { flow { emit(model) } },
      addValue = { called = it })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.model.collect()
    }

    val expected = TrackedProgressValue(progressId = model.id, value = 3, addedDate = Date(2))
    viewmodel.addNewValue(value = expected.value, date = expected.addedDate)

    assertEquals(expected, called)
  }
}