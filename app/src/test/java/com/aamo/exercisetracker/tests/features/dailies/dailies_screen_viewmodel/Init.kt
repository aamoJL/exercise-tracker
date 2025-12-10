package com.aamo.exercisetracker.tests.features.dailies.dailies_screen_viewmodel

import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.dailies.DailiesScreenViewModel
import com.aamo.exercisetracker.features.dailies.WeeklySchedule
import com.aamo.exercisetracker.features.dailies.models.DailiesRoutineModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Init {
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `weekly schedule set`() = runTest(UnconfinedTestDispatcher()) {
    val dataFlow = MutableSharedFlow<WeeklySchedule>()
    val viewmodel = DailiesScreenViewModel(
      fetchWeeklySchedule = { dataFlow },
      fetchTrackedProgresses = { flow { } })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.weeklySchedule.collect()
    }

    assertNull(viewmodel.weeklySchedule.value)

    val schedule = listOf(
      listOf(
        DailiesRoutineModel(
          routine = Routine(name = "123"),
          progress = DailiesRoutineModel.Progress(finishedCount = 0, totalCount = 1)
        )
      )
    )

    dataFlow.emit(schedule)

    assertEquals(schedule, viewmodel.weeklySchedule.value)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `tracked progresses set`() = runTest(UnconfinedTestDispatcher()) {
    val dataFlow = MutableSharedFlow<List<TrackedProgress>>()
    val viewmodel = DailiesScreenViewModel(
      fetchWeeklySchedule = { flow { } },
      fetchTrackedProgresses = { dataFlow })

    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewmodel.trackedProgresses.collect()
    }

    assertNull(viewmodel.trackedProgresses.value)

    val progresses = listOf(
      TrackedProgress(name = "1"),
      TrackedProgress(name = "2"),
    )

    dataFlow.emit(progresses)

    assertEquals(progresses, viewmodel.trackedProgresses.value)
  }
}