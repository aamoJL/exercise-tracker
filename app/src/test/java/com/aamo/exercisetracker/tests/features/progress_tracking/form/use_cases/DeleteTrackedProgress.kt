package com.aamo.exercisetracker.tests.features.progress_tracking.form.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.form.use_cases.deleteTrackedProgress
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class DeleteTrackedProgress : DatabaseTest() {
  @Test
  fun `progress deleted`() = runTest {
    val model = TrackedProgress(name = "Progress 1").let {
      trackedProgressDao.upsert(it).let { id -> it.copy(id = id) }
    }

    assertEquals(model, trackedProgressDao.getTrackedProgress(model.id))

    deleteTrackedProgress(dao = trackedProgressDao, model = model)
    assertNull(trackedProgressDao.getTrackedProgress(model.id))
  }
}