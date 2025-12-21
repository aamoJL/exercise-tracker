package com.aamo.exercisetracker.tests.features.progress_tracking.records.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.features.progress_tracking.records.models.TrackedProgressRecordListModel
import com.aamo.exercisetracker.features.progress_tracking.records.use_cases.fetchTrackedProgressRecordsFlow
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class FetchTrackedProgressRecords : DatabaseTest() {
  @Test
  fun `returns correct model`() = runTest {
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

    val expected = TrackedProgressRecordListModel(
      progressName = progress.name,
      valueUnit = progress.unit,
      records = records.sortedByDescending { it.addedDate },
      valueType = TrackedProgressRecordListModel.ValueType.COUNT
    )
    val actual =
      fetchTrackedProgressRecordsFlow(dao = trackedProgressDao, progressId = progress.id).first()

    assertEquals(expected, actual)
  }
}