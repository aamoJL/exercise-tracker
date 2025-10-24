package com.aamo.exercisetracker.tests.features.routine.use_cases

import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.features.routine.RoutineListScreenViewModel
import com.aamo.exercisetracker.features.routine.use_cases.fromDao
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Test

@Suppress("HardCodedStringLiteral")
class FetchRoutines {
  @Test
  fun `returns correct models from dao`() {
    val model = RoutineWithSchedule(
      routine = Routine(id = 0L, name = "Name"),
      schedule = RoutineSchedule(routineId = 0L, sunday = true)
    )
    val expected = listOf(
      RoutineListScreenViewModel.RoutineModel(
        routine = model.routine, schedule = model.schedule, isSelected = false
      )
    )

    val result = runBlocking {
      RoutineListScreenViewModel.RoutineModel.fromDao {
        flow { emit(listOf(model)) }
      }.first()
    }

    assertEquals(expected, result)
  }
}