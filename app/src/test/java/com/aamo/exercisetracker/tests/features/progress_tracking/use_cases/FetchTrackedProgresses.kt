package com.aamo.exercisetracker.tests.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.TrackedProgressListScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.use_cases.fetchTrackedProgressesFlow
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FetchTrackedProgresses {
  @Test
  fun `returns correct models`() = runBlocking {
    val items = listOf(TrackedProgress(id = 1L), TrackedProgress(id = 2L), TrackedProgress(id = 3L))
    val expected = items.map {
      TrackedProgressListScreenViewModel.ProgressModel(progress = it, isSelected = false)
    }

    val result = fetchTrackedProgressesFlow(fetchData = { flow { emit(items) } }).first()

    assertEquals(expected, result)
  }
}