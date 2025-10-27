package com.aamo.exercisetracker.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.ExerciseProgress
import java.util.Date

suspend fun saveExerciseProgress(
  finishedDate: Date,
  progress: ExerciseProgress,
  saveData: suspend (ExerciseProgress) -> Boolean,
): Boolean {
  return saveData(progress.copy(finishedDate = finishedDate))
}