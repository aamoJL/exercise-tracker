package com.aamo.exercisetracker.features.exercise.form.use_cases

import com.aamo.exercisetracker.database.dao.RoutineDao
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets

suspend fun fetchExercise(
  dao: RoutineDao, exerciseId: Long, routineId: Long, defaultUnit: String
): ExerciseWithSets? {
  return if (exerciseId == 0L) ExerciseWithSets(
    exercise = Exercise(routineId = routineId),
    sets = listOf(ExerciseSet(exerciseId = exerciseId, unit = defaultUnit))
  )
  else dao.getExerciseWithSets(exerciseId = exerciseId)?.let { result ->
    result.copy(sets = result.sets.sortedBy { it.id })
  }
}