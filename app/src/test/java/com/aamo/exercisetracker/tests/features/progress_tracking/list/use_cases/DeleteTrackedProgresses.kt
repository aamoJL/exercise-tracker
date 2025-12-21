package com.aamo.exercisetracker.tests.features.progress_tracking.list.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.list.use_cases.deleteTrackedProgresses
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class DeleteTrackedProgresses : DatabaseTest() {
  @Test
  fun `deletes progresses`() = runTest {
    val progresses = listOf(
      TrackedProgress(name = "Progress 1"),
      TrackedProgress(name = "Progress 2"),
      TrackedProgress(name = "Progress 3"),
    ).map { item ->
      trackedProgressDao.upsert(item).let { item.copy(id = it) }
    }

    TestCase.assertEquals(progresses, trackedProgressDao.getProgressesFlow().first())

    deleteTrackedProgresses(dao = trackedProgressDao, *progresses.toTypedArray())

    val actual = trackedProgressDao.getProgressesFlow().first()

    TestCase.assertTrue(actual.isEmpty())
  }
}