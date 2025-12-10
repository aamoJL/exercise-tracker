package com.aamo.exercisetracker.features.routine.list.use_cases

import com.aamo.exercisetracker.database.dao.RoutineDao
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun fetchRoutinesFlow(dao: RoutineDao): Flow<List<RoutineWithSchedule>> {
  return dao.getRoutinesWithScheduleFlow().map { list ->
    list.sortedBy { it.routine.name }
  }
}