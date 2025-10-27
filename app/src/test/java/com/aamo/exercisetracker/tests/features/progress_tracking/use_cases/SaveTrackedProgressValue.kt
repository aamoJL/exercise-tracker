package com.aamo.exercisetracker.tests.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgressValue
import com.aamo.exercisetracker.features.progress_tracking.use_cases.saveTrackedProgressValue
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.Date

class SaveTrackedProgressValue {
  @Test
  fun `returns correct model when saving new`() = runBlocking {
    var result: TrackedProgressValue? = null
    val value = TrackedProgressValue(id = 0L, progressId = 1L, value = 20, addedDate = Date(10))

    assert(
      saveTrackedProgressValue(
        value = value,
        saveData = { model ->
          result = model
          true
        },
      )
    )

    assertEquals(result, value)
  }

  @Test
  fun `returns false when did not save`() = runBlocking {
    val value = TrackedProgressValue(id = 0L, progressId = 1L, value = 20, addedDate = Date(10))

    assertFalse(
      saveTrackedProgressValue(
        value = value,
        saveData = { model ->
          false
        },
      )
    )
  }
}