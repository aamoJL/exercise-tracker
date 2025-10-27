@file:Suppress("HardCodedStringLiteral")

package com.aamo.exercisetracker.tests.features.routine.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.ExerciseWithProgress
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithExerciseProgresses
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.features.routine.RoutineFormViewModel
import com.aamo.exercisetracker.features.routine.use_cases.fetchRoutineWithSetsAndProgressesFlow
import com.aamo.exercisetracker.features.routine.use_cases.fromDao
import com.aamo.exercisetracker.utility.extensions.date.Day
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FetchRoutine {
  @Test
  fun `returns correct model from dao when new`() {
    val model = RoutineWithSchedule(
      routine = Routine(id = 0L, name = "Name"),
      schedule = RoutineSchedule(routineId = 0L, sunday = true)
    )
    val expected = RoutineFormViewModel.Model(
      routineName = model.routine.name, selectedDays = listOf(Day.SUNDAY), isNew = true
    )

    val result = runBlocking { RoutineFormViewModel.Model.fromDao { model } }

    assertEquals(expected, result)
  }

  @Test
  fun `returns correct model from dao when existing`() {
    val model = RoutineWithSchedule(
      routine = Routine(id = 1L, name = "Name"),
      schedule = RoutineSchedule(routineId = 0L, sunday = true)
    )
    val expected = RoutineFormViewModel.Model(
      routineName = model.routine.name, selectedDays = listOf(Day.SUNDAY), isNew = false
    )

    val result = runBlocking { RoutineFormViewModel.Model.fromDao { model } }

    assertEquals(expected, result)
  }

  @Test
  fun `fetches correct flow`() {
    val expected = RoutineWithExerciseProgresses(
      routine = Routine(id = 1L, name = "Name"), exerciseProgresses = listOf(
        ExerciseWithProgress(
          exercise = Exercise(id = 1L, routineId = 1L), progress = ExerciseProgress(exerciseId = 1L)
        )
      )
    )

    val result = runBlocking {
      fetchRoutineWithSetsAndProgressesFlow {
        flow { emit(expected) }
      }.first()
    }

    assertEquals(expected, result)
  }
}