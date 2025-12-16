package com.aamo.exercisetracker.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.features.exercise.ExerciseScreenViewModel
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import kotlin.time.Duration.Companion.milliseconds

suspend fun ExerciseScreenViewModel.Model.Companion.fromDao(
  fetchData: suspend () -> ExerciseWithSets
): ExerciseScreenViewModel.Model {
  return fetchData().let { (exercise, sets) ->
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