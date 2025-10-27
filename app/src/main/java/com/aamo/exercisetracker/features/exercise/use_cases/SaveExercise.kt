package com.aamo.exercisetracker.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.features.exercise.ExerciseFormViewModel
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.extensions.general.letIf
import kotlin.time.Duration.Companion.minutes

suspend fun saveExercise(
  data: ExerciseWithSets,
  saveData: suspend (ExerciseWithSets) -> Boolean,
): Boolean {
  return saveData(data)
}

fun ExerciseFormViewModel.Model.toDao(
  exerciseId: Long, routineId: Long
): ExerciseWithSets {
  val exercise = Exercise(
    id = exerciseId,
    routineId = routineId,
    name = this.exerciseName,
    restDuration = this.restDuration
  )
  val sets = this.setAmounts.map { amount ->
    ExerciseSet(
      id = 0L, exerciseId = exerciseId, value = amount.letIf(this.hasTimer) {
      // Change minutes to milliseconds if set has timer
      it.minutes.inWholeMilliseconds.toInt()
    }, unit = this.setUnit, valueType = ifElse(condition = this.hasTimer, ifTrue = {
      ExerciseSet.ValueType.COUNTDOWN
    }, ifFalse = {
      ExerciseSet.ValueType.REPETITION
    }))
  }

  return ExerciseWithSets(exercise = exercise, sets = sets)
}