package com.aamo.exercisetracker.features.dailies.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.Date

@Suppress("HardCodedStringLiteral")
class FetchUnfinishedTrackedProgresses {
  @Test
  fun `returns correct items`() = runBlocking {
    val currentTimeMillis = 10L
    val trackedProgresses = mapOf(
      TrackedProgress(id = 0, name = "Progress", intervalWeeks = 1) to listOf(
        TrackedProgressValue(progressId = 0, addedDate = Date(currentTimeMillis))
      ), TrackedProgress(
        id = 1, name = "Unfinished Progress", intervalWeeks = 1
      ) to emptyList(), TrackedProgress(
        id = 2, name = "Unscheduled Progress", intervalWeeks = 0
      ) to emptyList()
    )
    val result = fetchUnfinishedTrackedProgressesFlow(
      getDataFlow = {
        flow {
          emit(trackedProgresses)
        }
      }, currentTimeMillis = currentTimeMillis
    ).first()

    assertEquals(result.size, 1)
    assertEquals(result[0], trackedProgresses.toList().first { it.first.id == 1L }.first)
  }
}