package com.aamo.exercisetracker.tests.features.exercise.form.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.features.exercise.form.use_cases.saveExercise
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class SaveRoutine() : DatabaseTest() {
  @Test
  fun `returns correct id when saved`() = runTest {
    val model = Exercise(routineId = routineDao.upsert(Routine(name = "Routine 1")))
    val set = ExerciseSet(
      exerciseId = 0L, value = 123, unit = "Unit", valueType = ExerciseSet.ValueType.REPETITION
    )

    val id =
      saveExercise(dao = routineDao, model = ExerciseWithSets(exercise = model, sets = listOf(set)))
    assertNotEquals(model.id, id)

    val result = routineDao.getExerciseWithSets(id)

    checkNotNull(result)
    assertEquals(model.copy(id = id), result.exercise)
    assertEquals(listOf(set.copy(id = 1L, exerciseId = id)), result.sets)
  }
}