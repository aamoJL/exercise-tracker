package com.aamo.exercisetracker.tests.features.progress_tracking.list.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.list.use_cases.fetchTrackedProgressesFlow
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class FetchTrackedProgresses : DatabaseTest() {
  @Test
  fun `returns correct progresses`() = runTest {
    val progresses = listOf(
      TrackedProgress(name = "Progress 1"),
      TrackedProgress(name = "Progress 3"),
      TrackedProgress(name = "Progress 2"),
    ).map { item ->
      trackedProgressDao.upsert(item).let { item.copy(id = it) }
    }

    val expected = progresses.sortedBy { it.name }
    val actual = fetchTrackedProgressesFlow(dao = trackedProgressDao).first()

    assertEquals(expected, actual)
  }
}