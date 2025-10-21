package com.aamo.exercisetracker.tests.features.progress_tracking.use_cases

import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.use_cases.deleteTrackedProgress
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Test

class DeleteTrackedProgress {
  @Test
  fun `returns true when deleted`() = runBlocking {
    assert(deleteTrackedProgress(TrackedProgress(), deleteData = { true }))
  }

  @Test
  fun `returns true when deleted multiple`() = runBlocking {
    assert(deleteTrackedProgress(TrackedProgress(), deleteData = { true }))
  }

  @Test
  fun `returns false when not deleted`() = runBlocking {
    assertFalse(deleteTrackedProgress(TrackedProgress(), deleteData = { false }))
  }
}