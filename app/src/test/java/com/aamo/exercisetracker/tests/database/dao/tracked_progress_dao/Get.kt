package com.aamo.exercisetracker.tests.database.dao.tracked_progress_dao

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.database.entities.TrackedProgressWithValues
import com.aamo.exercisetracker.test_utility.database.TrackedProgressDatabaseTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class Get : TrackedProgressDatabaseTest() {
  @Test
  fun getTrackedProgress() = runTest {
    val progress = TrackedProgress()

    dao.upsert(progress).also { progressId ->
      val expected = progress.copy(id = progressId)
      val actual = dao.getTrackedProgress(progressId)

      assertEquals(expected, actual)
    }
  }

  @Test
  fun getProgressWithValuesFlow() = runTest {
    val progress = TrackedProgress()

    dao.upsert(progress).also { progressId ->
      val value = TrackedProgressValue(progressId = progressId, addedDate = Date())

      dao.upsert(value).also { valueId ->
        val expected = TrackedProgressWithValues(
          trackedProgress = progress.copy(id = progressId),
          values = listOf(value.copy(id = valueId))
        )
        val actual = dao.getProgressWithValuesFlow(progressId).first()

        assertEquals(expected, actual)
      }
    }
  }

  @Test
  fun getProgressesFlow() = runTest {
    val progresses = listOf(
      TrackedProgress(),
      TrackedProgress(),
      TrackedProgress(),
    )

    progresses.forEach {
      dao.upsert(it)
    }

    val expected = progresses.mapIndexed { i, progress -> progress.copy(id = i + 1L) }
    val actual = dao.getProgressesFlow().first()

    assertEquals(expected, actual)
  }

  @Test
  fun getProgressValuesFlow() = runTest {
    val progressId = dao.upsert(TrackedProgress())

    val values = listOf(
      TrackedProgressValue(progressId = progressId, addedDate = Date()),
      TrackedProgressValue(progressId = progressId, addedDate = Date()),
      TrackedProgressValue(progressId = progressId, addedDate = Date()),
    )

    values.forEach {
      dao.upsert(it)
    }

    val expected = values.mapIndexed { i, progress -> progress.copy(id = i + 1L) }
    val actual = dao.getProgressValuesFlow(progressId).first()

    assertEquals(expected, actual)
  }

  @Test
  fun getProgressValueById() = runTest {
    val progressId = dao.upsert(TrackedProgress())

    val value = TrackedProgressValue(progressId = progressId, addedDate = Date())

    dao.upsert(value).also { valueId ->
      val expected = value.copy(id = valueId)
      val actual = dao.getProgressValueById(valueId)

      assertEquals(expected, actual)
    }
  }

  @Test
  fun getProgressesWithValuesFlow() = runTest {
    val progress = TrackedProgress()

    dao.upsert(progress).also { progressId ->
      val value = TrackedProgressValue(progressId = progressId, addedDate = Date())

      dao.upsert(value).also { valueId ->
        val expected = mapOf(progress.copy(id = progressId) to listOf(value.copy(id = valueId)))
        val actual = dao.getProgressesWithValuesFlow().first()

        assertEquals(expected, actual)
      }
    }
  }
}