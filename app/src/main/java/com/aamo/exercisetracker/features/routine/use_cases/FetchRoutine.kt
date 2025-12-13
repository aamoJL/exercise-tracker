package com.aamo.exercisetracker.features.routine.use_cases

import com.aamo.exercisetracker.database.entities.RoutineWithExerciseProgresses
import kotlinx.coroutines.flow.Flow

fun fetchRoutineWithSetsAndProgressesFlow(
  fetchData: () -> Flow<RoutineWithExerciseProgresses?>
): Flow<RoutineWithExerciseProgresses?> {
  return fetchData()
}