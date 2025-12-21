package com.aamo.exercisetracker.tests.features.dailies.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.features.dailies.use_cases.fetchUnfinishedTrackedProgressesFlow
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class FetchUnfinishedTrackedProgresses : DatabaseTest() {
  @Test
  fun `returns correct items`() = runBlocking {
    val currentTimeMillis = 10L

    TrackedProgress(name = "Progress", intervalWeeks = 1).also {
      trackedProgressDao.upsert(it).let { id ->
        trackedProgressDao.upsert(
          TrackedProgressValue(progressId = id, addedDate = Date(currentTimeMillis))
        )
        it.copy(id = id)
      }
    }
    val unfinishedProgress = TrackedProgress(name = "Unfinished Progress", intervalWeeks = 1).let {
      trackedProgressDao.upsert(it).let { id -> it.copy(id = id) }
    }
    TrackedProgress(name = "Unscheduled Progress", intervalWeeks = 0).also {
      trackedProgressDao.upsert(it).let { id -> it.copy(id = id) }
    }

    val result = fetchUnfinishedTrackedProgressesFlow(
      dao = trackedProgressDao, currentTimeMillis = currentTimeMillis
    ).first()

    assertEquals(result.size, 1)
    assertEquals(result[0], unfinishedProgress)
  }
}