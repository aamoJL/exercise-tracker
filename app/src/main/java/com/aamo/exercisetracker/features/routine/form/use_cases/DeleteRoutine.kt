package com.aamo.exercisetracker.features.routine.form.use_cases

import com.aamo.exercisetracker.database.dao.RoutineDao
import com.aamo.exercisetracker.database.entities.Routine

suspend fun deleteRoutine(dao: RoutineDao, model: Routine) {
  dao.delete(model)
}