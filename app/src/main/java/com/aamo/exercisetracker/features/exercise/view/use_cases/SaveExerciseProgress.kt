package com.aamo.exercisetracker.features.exercise.view.use_cases

import com.aamo.exercisetracker.database.dao.RoutineDao
import com.aamo.exercisetracker.database.entities.ExerciseProgress

suspend fun saveExerciseProgress(dao: RoutineDao, progress: ExerciseProgress) {
  (dao.getExerciseProgressByExerciseId(progress.exerciseId)
    ?.copy(finishedDate = progress.finishedDate) ?: progress).also {
    dao.upsert(it)
  }
}