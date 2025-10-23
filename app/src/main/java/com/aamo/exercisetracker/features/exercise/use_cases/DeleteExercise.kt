package com.aamo.exercisetracker.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.Exercise

suspend fun deleteExercise(
  fetchData: suspend () -> Exercise?,
  deleteData: suspend (Exercise) -> Boolean,
): Boolean {
  return deleteData(fetchData() ?: throw Exception("Failed to fetch data"))
}