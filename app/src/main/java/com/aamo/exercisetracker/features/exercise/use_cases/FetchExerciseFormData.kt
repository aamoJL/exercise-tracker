package com.aamo.exercisetracker.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.features.exercise.ExerciseFormViewModel
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import com.aamo.exercisetracker.utility.extensions.general.letIf
import kotlin.time.Duration.Companion.milliseconds

suspend fun fetchExerciseFormData(
  id: Long, fetchData: suspend (id: Long) -> ExerciseWithSets?
): ExerciseFormViewModel.Model {
  return if (id == 0L) newModel() else existingModel(id = id, fetchExerciseData = fetchData)
}

private fun newModel(): ExerciseFormViewModel.Model {
  return ExerciseFormViewModel.Model(
    exerciseName = String.EMPTY,
    restDuration = 0.milliseconds,
    setUnit = String.EMPTY,
    setAmounts = listOf(0),
    hasTimer = false,
    isNew = true
  )
}

private suspend fun existingModel(
  id: Long,
  fetchExerciseData: suspend (id: Long) -> ExerciseWithSets?,
): ExerciseFormViewModel.Model {
  return (fetchExerciseData(id)
    ?: throw Exception("Failed to fetch data")).let { (exercise, sets) ->
    ExerciseFormViewModel.Model(
      exerciseName = exercise.name,
      restDuration = exercise.restDuration,
      setUnit = sets.firstOrNull()?.unit ?: String.EMPTY,
      setAmounts = sets.map { set ->
        set.value.letIf(set.valueType == ExerciseSet.ValueType.COUNTDOWN) {
          // Change milliseconds to minutes, if set has timer
          it.milliseconds.inWholeMinutes.toInt()
        }
      },
      hasTimer = (sets.firstOrNull()?.valueType == ExerciseSet.ValueType.COUNTDOWN),
      isNew = false
    )
  }
}