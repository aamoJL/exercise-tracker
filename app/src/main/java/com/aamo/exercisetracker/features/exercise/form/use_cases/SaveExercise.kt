package com.aamo.exercisetracker.features.exercise.form.use_cases

import com.aamo.exercisetracker.database.dao.RoutineDao
import com.aamo.exercisetracker.database.entities.ExerciseWithSets

suspend fun saveExercise(dao: RoutineDao, model: ExerciseWithSets): Long {
  return dao.upsert(exercise = model.exercise, sets = model.sets)
}