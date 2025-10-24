package com.aamo.exercisetracker.features.routine.use_cases

import com.aamo.exercisetracker.database.entities.RoutineWithExerciseProgresses
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.features.routine.RoutineFormViewModel
import kotlinx.coroutines.flow.Flow

fun fetchRoutineWithSetsAndProgressesFlow(
  fetchData: () -> Flow<RoutineWithExerciseProgresses?>
): Flow<RoutineWithExerciseProgresses?> {
  return fetchData()
}

suspend fun RoutineFormViewModel.Model.Companion.fromDao(
  fetchData: suspend () -> RoutineWithSchedule
): RoutineFormViewModel.Model {
  return fetchData().let { (routine, schedule) ->
    RoutineFormViewModel.Model(
      routineName = routine.name,
      selectedDays = schedule?.asListOfDays() ?: emptyList(),
      isNew = routine.id == 0L
    )
  }
}