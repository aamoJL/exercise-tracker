package com.aamo.exercisetracker.tests.features.routine.list.use_cases

import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.features.routine.list.use_cases.fetchRoutinesFlow
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class FetchRoutines : DatabaseTest() {
  @Test
  fun `returns correct routines`() = runTest {
    val routines = listOf(Routine(name = "Routine 2").let {
      routineDao.upsert(it).let { id -> it.copy(id = id) }
    }, Routine(name = "Routine 1").let {
      routineDao.upsert(it).let { id -> it.copy(id = id) }
    })

    val expected = routines.sortedBy { it.name }
    val actual = fetchRoutinesFlow(dao = routineDao).first().map { it.routine }

    TestCase.assertEquals(expected, actual)
  }
}