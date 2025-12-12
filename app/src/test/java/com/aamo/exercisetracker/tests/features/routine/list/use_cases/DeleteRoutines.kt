package com.aamo.exercisetracker.tests.features.routine.list.use_cases

import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.features.routine.list.use_cases.deleteRoutines
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class DeleteRoutines : DatabaseTest() {
  @Test
  fun `deletes routines`() = runTest {
    assertTrue(routineDao.getRoutinesWithScheduleFlow().first().isEmpty())

    val routines = listOf(Routine(name = "Routine 1").let {
      routineDao.upsert(it).let { id -> it.copy(id = id) }
    }, Routine(name = "Routine 2").let {
      routineDao.upsert(it).let { id -> it.copy(id = id) }
    })

    assertEquals(routineDao.getRoutinesWithScheduleFlow().first().map { it.routine }, routines)

    deleteRoutines(dao = routineDao, *routines.toTypedArray())

    assertTrue(routineDao.getRoutinesWithScheduleFlow().first().isEmpty())
  }
}