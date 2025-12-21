package com.aamo.exercisetracker.tests.features.progress_tracking.form.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.form.use_cases.saveTrackedProgress
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class SaveTrackedProgress : DatabaseTest() {
  @Test
  fun `model saved`() = runTest {
    val model = TrackedProgress(name = "Progress 1").let {
      saveTrackedProgress(dao = trackedProgressDao, model = it).let { id -> it.copy(id = id) }
    }
    val result = trackedProgressDao.getTrackedProgress(model.id)

    assertEquals(model, result)
  }
}