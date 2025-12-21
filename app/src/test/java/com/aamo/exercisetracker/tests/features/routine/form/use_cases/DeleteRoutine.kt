package com.aamo.exercisetracker.tests.features.routine.form.use_cases

import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.features.routine.form.use_cases.deleteRoutine
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class DeleteRoutine : DatabaseTest() {
  @Test
  fun `deletes model`() = runTest {
    val routine = Routine(name = "Routine 1").let {
      routineDao.upsert(it).let { id -> it.copy(id = id) }
    }

    assertEquals(routine, routineDao.getRoutine(routine.id))

    deleteRoutine(dao = routineDao, model = routine)
    assertEquals(null, routineDao.getRoutine(routine.id))
  }
}