package com.aamo.exercisetracker.tests.features.routine.use_cases

import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.features.routine.use_cases.deleteRoutine
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test

class DeleteRoutine {
  @Test
  fun `returns correct model when deleting`() {
    val routines = listOf(
      Routine(id = 1L, name = "1"), Routine(id = 2L, name = "2"), Routine(id = 3L, name = "3")
    )
    var result: List<Routine>? = null

    assert(runBlocking {
      deleteRoutine(*routines.toTypedArray()) {
        result = it; true
      }
    })

    assertEquals(routines, result)
  }
}