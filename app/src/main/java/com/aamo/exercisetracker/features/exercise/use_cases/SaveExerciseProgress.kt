package com.aamo.exercisetracker.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.ExerciseProgress
import java.util.Date

suspend fun saveExerciseProgress(
  finishedDate: Date,
  fetchData: suspend () -> ExerciseProgress?,
  saveData: suspend (ExerciseProgress) -> Boolean,
): Boolean {
  return saveData(
    (fetchData() ?: throw Exception("Failed to fetch data")).copy(finishedDate = finishedDate)
  )
}