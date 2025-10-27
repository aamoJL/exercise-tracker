package com.aamo.exercisetracker.features.routine.use_cases

import com.aamo.exercisetracker.database.entities.Routine

suspend fun deleteRoutine(
  vararg model: Routine, deleteData: suspend (List<Routine>) -> Boolean
): Boolean {
  return deleteData(model.toList())
}