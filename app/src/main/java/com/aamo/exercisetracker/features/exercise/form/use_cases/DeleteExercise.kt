package com.aamo.exercisetracker.features.exercise.form.use_cases

import com.aamo.exercisetracker.database.dao.RoutineDao
import com.aamo.exercisetracker.database.entities.Exercise

suspend fun deleteExercise(dao: RoutineDao, model: Exercise) {
  dao.delete(model)
}