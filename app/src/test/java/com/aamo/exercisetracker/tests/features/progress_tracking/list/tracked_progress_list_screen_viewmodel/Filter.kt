package com.aamo.exercisetracker.tests.features.progress_tracking.list.tracked_progress_list_screen_viewmodel

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.list.TrackedProgressListScreenViewModel
import com.aamo.exercisetracker.test_utility.ui.rules.UnconfinedTest
import junit.framework.TestCase
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
class Filter : UnconfinedTest() {
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun filter() = runTest(UnconfinedTestDispatcher()) {
    val dataFlow = MutableSharedFlow<List<TrackedProgress>>()
    val viewmodel = TrackedProgressListScreenViewModel(fetchData = { dataFlow }, deleteData = {})

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.filteredProgresses.collect()
    }

    val progresses = listOf(
      TrackedProgress(name = "A"),
      TrackedProgress(name = "A"),
      TrackedProgress(name = "B"),
      TrackedProgress(name = "C"),
    ).also {
      dataFlow.emit(it)
    }

    TestCase.assertEquals(progresses, viewmodel.filteredProgresses.value)

    val filterWord = progresses.first().name
    viewmodel.setFilterWord(filterWord)

    TestCase.assertEquals(filterWord, viewmodel.filterWord.value)
    TestCase.assertEquals(
      progresses.filter { it.name == filterWord }, viewmodel.filteredProgresses.value
    )
  }
}