package com.aamo.exercisetracker.tests.features.progress_tracking.view.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.features.progress_tracking.view.models.ProgressTrackingTrackedProgressModel
import com.aamo.exercisetracker.features.progress_tracking.view.use_cases.fetchTrackedProgressFlow
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
class FetchTrackedProgress : DatabaseTest() {
  @Test
  fun `returns correct model`() = runTest {
    val model = TrackedProgress(name = "Progress 1").let {
      trackedProgressDao.upsert(it).let { id -> it.copy(id = id) }
    }
    val records = listOf(
      TrackedProgressValue(progressId = model.id, addedDate = Date(2)),
      TrackedProgressValue(progressId = model.id, addedDate = Date(3)),
      TrackedProgressValue(progressId = model.id, addedDate = Date(1)),
    ).map { record ->
      trackedProgressDao.upsert(record).let { id -> record.copy(id = id) }
    }

    val expected = ProgressTrackingTrackedProgressModel(
      id = model.id,
      name = model.name,
      values = records.sortedBy { it.addedDate }.map { it.value },
      recordUnit = model.unit,
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.REPETITION,
      countdownTime = null
    )
    val actual = fetchTrackedProgressFlow(dao = trackedProgressDao, progressId = model.id).first()

    assertEquals(expected, actual)
  }
}