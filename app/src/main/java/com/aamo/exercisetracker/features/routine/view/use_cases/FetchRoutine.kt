package com.aamo.exercisetracker.features.routine.view.use_cases

import com.aamo.exercisetracker.database.dao.RoutineDao
import com.aamo.exercisetracker.database.entities.RoutineWithExerciseProgresses
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun fetchRoutineFlow(dao: RoutineDao, routineId: Long): Flow<RoutineWithExerciseProgresses?> {
  return dao.getRoutineWithProgressesFlow(routineId = routineId).map { item ->
    item?.copy(exerciseProgresses = item.exerciseProgresses.sortedBy { it.exercise.name })
  }
}