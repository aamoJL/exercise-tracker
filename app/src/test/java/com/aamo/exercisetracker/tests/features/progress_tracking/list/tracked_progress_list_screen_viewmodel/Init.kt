package com.aamo.exercisetracker.tests.features.progress_tracking.list.tracked_progress_list_screen_viewmodel

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.list.TrackedProgressListScreenViewModel
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@Suppress("HardCodedStringLiteral")
class Init : UnconfinedTest() {
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `filtered progresses set`() = runTest(UnconfinedTestDispatcher()) {
    val dataFlow = MutableSharedFlow<List<TrackedProgress>>()
    val viewmodel = TrackedProgressListScreenViewModel(fetchData = { dataFlow }, deleteData = {})

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.filteredProgresses.collect()
    }

    assertNull(viewmodel.filteredProgresses.value)

    val progresses = listOf(TrackedProgress(name = "Progress 1")).also {
      dataFlow.emit(it)
    }

    assertEquals(progresses, viewmodel.filteredProgresses.value)
  }

  @Test
  fun `filter word is empty`() {
    val viewmodel = TrackedProgressListScreenViewModel(fetchData = { flow { } }, deleteData = {})

    assertEquals(String.EMPTY, viewmodel.filterWord.value)
  }

  @Test
  fun `selections is empty`() {
    val viewmodel = TrackedProgressListScreenViewModel(fetchData = { flow { } }, deleteData = {})

    assertTrue(viewmodel.selections.value.isEmpty())
  }
}