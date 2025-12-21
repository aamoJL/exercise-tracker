package com.aamo.exercisetracker.tests.features.progress_tracking.records.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.features.progress_tracking.records.use_cases.saveTrackedProgressRecord
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
class SaveTrackedProgressRecord : DatabaseTest() {
  @Test
  fun `model saved`() = runTest {
    val progress = TrackedProgress(name = "Progress 1").let {
      trackedProgressDao.upsert(it).let { id -> it.copy(id = id) }
    }
    val record = TrackedProgressValue(progressId = progress.id, addedDate = Date(1))

    saveTrackedProgressRecord(dao = trackedProgressDao, record = record)
    TestCase.assertEquals(
      listOf(record.copy(id = 1L)),
      trackedProgressDao.getProgressValuesFlow(progressId = progress.id).first()
    )
  }
}