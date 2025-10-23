package com.aamo.exercisetracker.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.Exercise

suspend fun deleteExercise(
  exercise: Exercise,
  deleteData: suspend (Exercise) -> Boolean,
): Boolean {
  return deleteData(exercise)
}