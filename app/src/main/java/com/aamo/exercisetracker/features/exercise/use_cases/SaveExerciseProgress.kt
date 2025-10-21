package com.aamo.exercisetracker.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.ExerciseProgress
import java.util.Date

suspend fun saveExerciseProgress(
  exerciseId: Long,
  finishedDate: Date,
  fetchData: suspend () -> ExerciseProgress?,
  saveData: suspend (ExerciseProgress) -> Boolean,
): Boolean {
  return saveData(
    fetchData()?.copy(finishedDate = finishedDate) ?: ExerciseProgress(
      exerciseId = exerciseId, finishedDate = finishedDate
    )
  )
}