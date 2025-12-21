package com.aamo.exercisetracker.features.exercise.view.use_cases

import com.aamo.exercisetracker.database.dao.RoutineDao
import com.aamo.exercisetracker.database.entities.ExerciseWithSets

suspend fun fetchExercise(dao: RoutineDao, exerciseId: Long): ExerciseWithSets? {
  return dao.getExerciseWithSets(exerciseId)?.let { result ->
    result.copy(sets = result.sets.sortedBy { it.id })
  }
}