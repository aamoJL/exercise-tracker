package com.aamo.exercisetracker.tests.features.progress_tracking.form.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.form.use_cases.fetchTrackedProgress
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class FetchTrackedProgress : DatabaseTest() {
  @Test
  fun `returns correct model when new`() = runTest {
    val expected = TrackedProgress(unit = "Unit")
    val actual = fetchTrackedProgress(
      dao = trackedProgressDao, progressId = expected.id, defaultUnit = expected.unit
    )

    assertEquals(expected, actual)
  }

  @Test
  fun `returns correct model when existing`() = runTest {
    val expected = TrackedProgress(name = "Progress 1", unit = "Unit").let {
      trackedProgressDao.upsert(it).let { id -> it.copy(id = id) }
    }
    val actual = fetchTrackedProgress(
      dao = trackedProgressDao, progressId = expected.id, defaultUnit = expected.unit
    )

    assertEquals(expected, actual)
  }
}