package com.aamo.exercisetracker.tests.features.exercise.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.features.exercise.ExerciseFormViewModel
import com.aamo.exercisetracker.features.exercise.use_cases.ExerciseData
import com.aamo.exercisetracker.features.exercise.use_cases.saveExercise
import com.aamo.exercisetracker.features.exercise.use_cases.toDao
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
class SaveExercise {
  @Test
  fun `returns correct model when saving new`() {
    val model = ExerciseFormViewModel.Model(
      exerciseName = "Name",
      restDuration = 2.minutes,
      setUnit = "Unit",
      setAmounts = listOf(2),
      hasTimer = false,
      isNew = true
    )
    var result: ExerciseData? = null

    assert(runBlocking {
      saveExercise(
        data = model.toDao(exerciseId = 0L, routineId = 1L), saveData = { result = it; true })
    })

    assertEquals(
      ExerciseData(
        exercise = Exercise(
          id = 0L, routineId = 1L, name = model.exerciseName, restDuration = model.restDuration
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
  fun `returns correct model when saving existing`() {
    val model = ExerciseFormViewModel.Model(
      exerciseName = "New Name",
      restDuration = 1.minutes,
      setUnit = "New Unit",
      setAmounts = listOf(3),
      hasTimer = true,
      isNew = false
    )
    val existing = ExerciseData(
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
    var result: ExerciseData? = null

    assert(runBlocking {
      saveExercise(
        data = model.toDao(
          exerciseId = existing.exercise.id, routineId = existing.exercise.routineId
        ), saveData = { result = it; true })
    })

    assertEquals(
      ExerciseData(
        exercise = Exercise(
          id = existing.exercise.id,
          routineId = existing.exercise.routineId,
          name = model.exerciseName,
          restDuration = model.restDuration
        ), sets = listOf(
          ExerciseSet(
            id = 0L,
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