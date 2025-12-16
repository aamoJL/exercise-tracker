package com.aamo.exercisetracker.tests.features.exercise.form.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.features.exercise.form.use_cases.deleteExercise
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class DeleteExercise : DatabaseTest() {
  @Test
  fun `deletes model`() = runTest {
    val model = Exercise(routineId = routineDao.upsert(Routine(name = "Routine 1"))).let {
      routineDao.upsert(it).let { id -> it.copy(id = id) }
    }

    assertEquals(model, routineDao.getExercise(model.id))

    deleteExercise(dao = routineDao, model = model)
    assertEquals(null, routineDao.getExercise(model.id))
  }
}