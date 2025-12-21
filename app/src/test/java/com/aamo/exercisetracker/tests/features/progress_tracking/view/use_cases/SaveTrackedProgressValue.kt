package com.aamo.exercisetracker.tests.features.progress_tracking.view.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.features.progress_tracking.view.use_cases.saveTrackedProgressValue
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
class SaveTrackedProgressValue : DatabaseTest() {
  @Test
  fun `value saved`() = runTest {
    val model = TrackedProgress(name = "Progress 1").let {
      trackedProgressDao.upsert(it).let { id -> it.copy(id = id) }
    }

    val value = TrackedProgressValue(progressId = model.id, addedDate = Date(1))

    saveTrackedProgressValue(dao = trackedProgressDao, value = value)

    val result = trackedProgressDao.getProgressValuesFlow(progressId = model.id).first()

    assertEquals(listOf(value.copy(id = 1L)), result)
  }
}