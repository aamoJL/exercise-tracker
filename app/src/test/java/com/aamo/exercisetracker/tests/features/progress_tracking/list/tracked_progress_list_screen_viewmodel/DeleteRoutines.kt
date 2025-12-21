package com.aamo.exercisetracker.tests.features.progress_tracking.list.tracked_progress_list_screen_viewmodel

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.list.TrackedProgressListScreenViewModel
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@Suppress("HardCodedStringLiteral")
class DeleteRoutines : UnconfinedTest() {
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `deleteData called`() = runTest(UnconfinedTestDispatcher()) {
    var called: List<TrackedProgress>? = null
    val viewmodel =
      TrackedProgressListScreenViewModel(fetchData = { flow { } }, deleteData = { called = it })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.filteredProgresses.collect()
    }

    val progresses = listOf(TrackedProgress(name = "A"), TrackedProgress(name = "B"))

    viewmodel.deleteProgresses(progresses)

    assertEquals(progresses, called)
  }
}