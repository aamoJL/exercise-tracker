package com.aamo.exercisetracker.tests.features.routine.use_cases

import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.features.routine.RoutineFormViewModel
import com.aamo.exercisetracker.features.routine.use_cases.saveRoutine
import com.aamo.exercisetracker.features.routine.use_cases.toDao
import com.aamo.exercisetracker.utility.extensions.date.Day
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Test

@Suppress("HardCodedStringLiteral")
class SaveRoutine() {
  @Test
  fun `returns correct model when saving`() {
    val model = RoutineWithSchedule(
      routine = Routine(id = 1L, name = "Name"),
      schedule = RoutineSchedule(routineId = 1L, sunday = true)
    )
    var result: RoutineWithSchedule? = null

    assert(runBlocking { saveRoutine(model) { result = it; true } })

    TestCase.assertEquals(model, result)
  }

  @Test
  fun `returns correct dao model`() {
    val routineId = 2L
    val model = RoutineFormViewModel.Model(
      routineName = "Name", selectedDays = listOf(
        Day.MONDAY, Day.WEDNESDAY, Day.SATURDAY
      ), isNew = false
    )

    val expected = RoutineWithSchedule(
      routine = Routine(id = routineId, name = model.routineName),
      schedule = RoutineSchedule(
        routineId = routineId,
        monday = true,
        wednesday = true,
        saturday = true
      )
    )

    val result = model.toDao(routineId)

    TestCase.assertEquals(expected, result)
  }
}