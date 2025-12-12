package com.aamo.exercisetracker.tests.features.progress_tracking.list.tracked_progress_list_screen_viewmodel

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.list.TrackedProgressListScreenViewModel
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Selections : UnconfinedTest() {
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun selections() = runTest(UnconfinedTestDispatcher()) {
    val dataFlow = MutableSharedFlow<List<TrackedProgress>>()
    val viewmodel = TrackedProgressListScreenViewModel(fetchData = { dataFlow }, deleteData = {})

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.selections.collect()
    }

    val progresses = listOf(
      TrackedProgress(name = "A"),
      TrackedProgress(name = "B"),
      TrackedProgress(name = "C"),
      TrackedProgress(name = "D"),
    ).also {
      dataFlow.emit(it)
    }

    assertTrue(viewmodel.selections.value.isEmpty())

    // set true
    viewmodel.switchProgressSelection(models = listOf(progresses.first()), state = true)
    assertEquals(listOf(progresses.first()), viewmodel.selections.value)

    // set true on the same item
    viewmodel.switchProgressSelection(models = listOf(progresses.first()), state = true)
    assertEquals(listOf(progresses.first()), viewmodel.selections.value)

    // set false
    viewmodel.switchProgressSelection(models = listOf(progresses.first()), state = false)
    assertTrue(viewmodel.selections.value.isEmpty())
  }
}