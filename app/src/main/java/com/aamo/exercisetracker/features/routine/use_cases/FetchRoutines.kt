package com.aamo.exercisetracker.features.routine.use_cases

import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.features.routine.RoutineListScreenViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun RoutineListScreenViewModel.RoutineModel.Companion.fromDao(
  fetchData: () -> Flow<List<RoutineWithSchedule>>
): Flow<List<RoutineListScreenViewModel.RoutineModel>> {
  return fetchData().map { list ->
    list.map {
      RoutineListScreenViewModel.RoutineModel(routine = it.routine, schedule = it.schedule)
    }
  }
}