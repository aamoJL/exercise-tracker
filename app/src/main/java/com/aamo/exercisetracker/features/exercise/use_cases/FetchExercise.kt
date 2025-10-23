package com.aamo.exercisetracker.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.features.exercise.ExerciseFormViewModel
import com.aamo.exercisetracker.features.exercise.ExerciseScreenViewModel
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.extensions.general.letIf
import kotlin.time.Duration.Companion.milliseconds

suspend fun ExerciseScreenViewModel.Model.Companion.fromDao(
  fetchExercise: suspend () -> Exercise?, fetchSets: suspend (id: Long) -> List<ExerciseSet>
): ExerciseScreenViewModel.Model {
  return (fetchExercise() ?: throw Exception("Failed to fetch exercise")).let { exercise ->
    fetchSets(exercise.id).let { sets ->
      ExerciseScreenViewModel.Model(
        exerciseName = exercise.name, routineId = exercise.routineId, sets = sets.map { set ->
          ExerciseScreenViewModel.Model.SetModel(
            repetitions = ifElse(
              condition = set.valueType == ExerciseSet.ValueType.REPETITION,
              ifTrue = { set.value },
              ifFalse = { null }), setDuration = ifElse(
              condition = set.valueType == ExerciseSet.ValueType.COUNTDOWN,
              ifTrue = { set.value.milliseconds },
              ifFalse = { null }), restDuration = exercise.restDuration, unit = set.unit
          )
        })
    }
  }
}

suspend fun ExerciseFormViewModel.Model.Companion.fromDao(
  fetchExercise: suspend () -> Exercise?, fetchSets: suspend (id: Long) -> List<ExerciseSet>
): ExerciseFormViewModel.Model {
  return (fetchExercise() ?: throw Exception("Failed to fetch exercise")).let { exercise ->
    fetchSets(exercise.id).let { sets ->
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
        isNew = exercise.id == 0L
      )
    }
  }
}