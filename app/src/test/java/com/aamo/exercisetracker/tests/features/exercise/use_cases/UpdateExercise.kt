package com.aamo.exercisetracker.tests.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.features.exercise.ExerciseFormViewModel
import com.aamo.exercisetracker.features.exercise.use_cases.updateExercise
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
class UpdateExercise {
  @Test
  fun `returns correct model when saving new`() = runBlocking {
    val model = ExerciseFormViewModel.Model(
      exerciseName = "Name",
      restDuration = 2.minutes,
      setUnit = "Unit",
      setAmounts = listOf(2),
      hasTimer = false,
      isNew = true
    )
    var result: ExerciseWithSets? = null

    assert(
      updateExercise(
        exerciseId = 0L,
        routineId = 0L,
        model = model,
        fetchData = { null },
        saveData = { result = it; true })
    )

    TestCase.assertEquals(
      ExerciseWithSets(
        exercise = Exercise(
          id = 0L, routineId = 0L, name = model.exerciseName, restDuration = model.restDuration
        ), sets = model.setAmounts.map {
          ExerciseSet(
            id = 0L,
            exerciseId = 0L,
            value = it,
            unit = model.setUnit,
            valueType = ExerciseSet.ValueType.REPETITION
          )
        }), result
    )
  }

  @Test
  fun `returns correct model when saving existing`() = runBlocking {
    val model = ExerciseFormViewModel.Model(
      exerciseName = "New Name",
      restDuration = 1.minutes,
      setUnit = "New Unit",
      setAmounts = listOf(3),
      hasTimer = true,
      isNew = false
    )
    val existing = ExerciseWithSets(
      exercise = Exercise(
        id = 1L, routineId = 1L, name = "Name", restDuration = 2.minutes
      ), sets = listOf(
        ExerciseSet(
          id = 1L,
          exerciseId = 1L,
          value = 2,
          unit = "Unit",
          valueType = ExerciseSet.ValueType.REPETITION
        )
      )
    )
    var result: ExerciseWithSets? = null

    assert(
      updateExercise(
        exerciseId = existing.exercise.id,
        routineId = existing.exercise.routineId,
        model = model,
        fetchData = { existing },
        saveData = { result = it; true })
    )

    TestCase.assertEquals(
      ExerciseWithSets(
        exercise = Exercise(
          id = existing.exercise.id,
          routineId = existing.exercise.routineId,
          name = model.exerciseName,
          restDuration = model.restDuration
        ), sets = listOf(
          ExerciseSet(
            id = 1L,
            exerciseId = existing.exercise.id,
            value = 3.minutes.inWholeMilliseconds.toInt(),
            unit = model.setUnit,
            valueType = ExerciseSet.ValueType.COUNTDOWN
          )
        )
      ), result
    )
  }
}