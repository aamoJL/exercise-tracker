package com.aamo.exercisetracker.features.routine.list.use_cases

import com.aamo.exercisetracker.database.dao.RoutineDao
import com.aamo.exercisetracker.database.entities.Routine

suspend fun deleteRoutines(dao: RoutineDao, vararg models: Routine): Boolean {
  return dao.delete(*models) > 0
}