package com.aamo.exercisetracker.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.features.exercise.ExerciseFormViewModel
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.extensions.general.letIf
import kotlin.time.Duration.Companion.minutes

suspend fun saveExercise(
  exerciseId: Long,
  routineId: Long,
  model: ExerciseFormViewModel.Model,
  fetchData: suspend (id: Long) -> ExerciseWithSets?,
  saveData: suspend (ExerciseWithSets) -> Boolean
): Boolean {
  return saveData(
    if (exerciseId == 0L) newModel(routineId = routineId, model = model)
    else existingModel(
      exerciseId = exerciseId, model = model, fetchExerciseData = fetchData
    )
  )
}

private fun newModel(
  routineId: Long, model: ExerciseFormViewModel.Model
): ExerciseWithSets {
  return ExerciseWithSets(
    exercise = Exercise(
      routineId = routineId, name = model.exerciseName, restDuration = model.restDuration
    ), sets = model.setAmounts.map { amount ->
      ExerciseSet(
        value = amount.letIf(model.hasTimer) {
          // Change minutes to milliseconds if set has timer
          it.minutes.inWholeMilliseconds.toInt()
        }, unit = model.setUnit, valueType = ifElse(
          condition = model.hasTimer,
          ifTrue = { ExerciseSet.ValueType.COUNTDOWN },
          ifFalse = { ExerciseSet.ValueType.REPETITION }), exerciseId = 0L
      )
    })
}

private suspend fun existingModel(
  exerciseId: Long,
  model: ExerciseFormViewModel.Model,
  fetchExerciseData: suspend (id: Long) -> ExerciseWithSets?,
): ExerciseWithSets {
  return (fetchExerciseData(exerciseId)
    ?: throw Exception("Failed to fetch data")).let { (exercise, sets) ->
    ExerciseWithSets(
      exercise = exercise.copy(
        name = model.exerciseName, restDuration = model.restDuration
      ), sets = sets.take(model.setAmounts.size).let { list ->
        list.toMutableList().apply {
          // Add missing sets
          repeat(model.setAmounts.size - list.size) { add(ExerciseSet(exerciseId = exerciseId)) }
        }.let { list ->
          list.mapIndexed { i, item ->
            item.copy(
              exerciseId = exercise.id, value = model.setAmounts[i].letIf(model.hasTimer) {
                // Change minutes to milliseconds if set has timer
                it.minutes.inWholeMilliseconds.toInt()
              }, unit = model.setUnit, valueType = ifElse(
                condition = model.hasTimer,
                ifTrue = { ExerciseSet.ValueType.COUNTDOWN },
                ifFalse = { ExerciseSet.ValueType.REPETITION })
            )
          }
        }
      })
  }
}