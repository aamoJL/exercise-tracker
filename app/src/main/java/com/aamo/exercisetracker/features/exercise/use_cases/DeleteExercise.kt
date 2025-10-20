package com.aamo.exercisetracker.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.Exercise

suspend fun deleteExercise(
  exerciseId: Long,
  fetchData: suspend (id: Long) -> Exercise?,
  deleteData: suspend (Exercise) -> Boolean,
): Boolean {
  if (exerciseId == 0L) return false

  return (fetchData(exerciseId) ?: throw Exception("Failed to fetch data")).let { result ->
    (deleteData(result))
  }
}