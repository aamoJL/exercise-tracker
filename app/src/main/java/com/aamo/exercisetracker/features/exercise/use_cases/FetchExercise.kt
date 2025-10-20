package com.aamo.exercisetracker.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithProgressAndSets
import com.aamo.exercisetracker.features.exercise.ExerciseScreenViewModel
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import kotlin.time.Duration.Companion.milliseconds

suspend fun fetchExercise(
  fetchData: suspend () -> ExerciseWithProgressAndSets?
): ExerciseScreenViewModel.Model {
  return (fetchData() ?: throw Exception("Failed to fetch data")).let { result ->
    ExerciseScreenViewModel.Model(
      exerciseName = result.exercise.name,
      routineId = result.exercise.routineId,
      sets = result.sets.map { set ->
        ExerciseScreenViewModel.Model.SetModel(
          repetitions = ifElse(
            condition = set.valueType == ExerciseSet.ValueType.REPETITION,
            ifTrue = { set.value },
            ifFalse = { null }), setDuration = ifElse(
            condition = set.valueType == ExerciseSet.ValueType.COUNTDOWN,
            ifTrue = { set.value.milliseconds },
            ifFalse = { null }), restDuration = result.exercise.restDuration, unit = set.unit
        )
      })
  }
}