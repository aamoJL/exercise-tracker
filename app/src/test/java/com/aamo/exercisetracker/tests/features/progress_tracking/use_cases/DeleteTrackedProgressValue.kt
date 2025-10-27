package com.aamo.exercisetracker.tests.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.features.progress_tracking.TrackedProgressRecordListScreenViewModel
import com.aamo.exercisetracker.features.progress_tracking.use_cases.deleteTrackedProgressValue
import com.aamo.exercisetracker.features.progress_tracking.use_cases.toDao
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.Date

class DeleteTrackedProgressValue {
  @Test
  fun `returns correct model on delete`() {
    val values = listOf(
      TrackedProgressValue(progressId = 1L, addedDate = Date(1)),
      TrackedProgressValue(progressId = 1L, addedDate = Date(2)),
      TrackedProgressValue(progressId = 1L, addedDate = Date(3)),
      TrackedProgressValue(progressId = 1L, addedDate = Date(4)),
    )
    var result: List<TrackedProgressValue>? = null

    runBlocking {
      deleteTrackedProgressValue(*values.toTypedArray()) { list ->
        true.also { result = list }
      }
    }

    assertEquals(values, result)
  }

  @Test
  fun `returns correct model`() {
    val progressId = 2L
    val model =
      TrackedProgressRecordListScreenViewModel.RecordModel(value = 10, date = Date(10), key = 1)

    val result = model.toDao(progressId)

    assertEquals(
      TrackedProgressValue(
        id = model.key, progressId = progressId, value = model.value, addedDate = model.date
      ), result
    )
  }
}