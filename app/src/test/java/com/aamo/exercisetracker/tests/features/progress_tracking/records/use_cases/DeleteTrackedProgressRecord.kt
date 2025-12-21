package com.aamo.exercisetracker.tests.features.progress_tracking.records.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.features.progress_tracking.records.use_cases.deleteTrackedProgressRecord
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class DeleteTrackedProgressRecord : DatabaseTest() {
  @Test
  fun `model deleted`() = runTest {
    val progress = TrackedProgress(name = "Progress 1").let {
      trackedProgressDao.upsert(it).let { id -> it.copy(id = id) }
    }
    val records = listOf(
      TrackedProgressValue(progressId = progress.id, addedDate = Date(2)),
      TrackedProgressValue(progressId = progress.id, addedDate = Date(3)),
      TrackedProgressValue(progressId = progress.id, addedDate = Date(1)),
    ).map { record ->
      trackedProgressDao.upsert(record).let { id -> record.copy(id = id) }
    }

    deleteTrackedProgressRecord(dao = trackedProgressDao, record = records.first())
    TestCase.assertEquals(
      records.drop(1),
      trackedProgressDao.getProgressWithValuesFlow(progressId = progress.id).first()?.values
    )
  }
}