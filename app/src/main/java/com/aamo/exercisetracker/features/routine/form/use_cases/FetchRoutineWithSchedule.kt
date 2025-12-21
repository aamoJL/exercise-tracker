package com.aamo.exercisetracker.features.routine.form.use_cases

import com.aamo.exercisetracker.database.dao.RoutineDao
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule

suspend fun fetchRoutineWithSchedule(dao: RoutineDao, routineId: Long): RoutineWithSchedule? {
  return if (routineId == 0L) RoutineWithSchedule(routine = Routine(), schedule = null)
  else dao.getRoutineWithSchedule(routineId)
}