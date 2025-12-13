package com.aamo.exercisetracker.features.routine.form.use_cases

import com.aamo.exercisetracker.database.dao.RoutineDao
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule

suspend fun saveRoutine(dao: RoutineDao, routine: Routine, schedule: RoutineSchedule?): Long {
  return dao.upsert(routine, schedule).routineId
}