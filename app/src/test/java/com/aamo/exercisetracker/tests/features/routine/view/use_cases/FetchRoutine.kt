package com.aamo.exercisetracker.tests.features.routine.view.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.ExerciseWithProgress
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineWithExerciseProgresses
import com.aamo.exercisetracker.features.routine.view.use_cases.fetchRoutineFlow
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class FetchRoutine : DatabaseTest() {
  @Test
  fun `returns correct model`() = runTest {
    val routine = Routine(name = "Routine 1").let {
      routineDao.upsert(it).let { id -> it.copy(id = id) }
    }
    val progresses = listOf(
      ExerciseWithProgress(
        exercise = Exercise(routineId = routine.id, name = "Exercise 2"),
        progress = ExerciseProgress(exerciseId = 0L, finishedDate = Date(2))
      ),
      ExerciseWithProgress(
        exercise = Exercise(routineId = routine.id, name = "Exercise 3"),
        progress = ExerciseProgress(exerciseId = 0L)
      ),
      ExerciseWithProgress(
        exercise = Exercise(routineId = routine.id, name = "Exercise 1"),
        progress = ExerciseProgress(exerciseId = 0L, finishedDate = Date(1))
      ),
    ).map { (exercise, progress) ->
      routineDao.upsert(exercise).let { eId ->
        val pId = progress?.copy(exerciseId = eId)?.let { routineDao.upsert(it) }

        ExerciseWithProgress(
          exercise = exercise.copy(id = eId),
          progress = progress?.copy(id = pId ?: error("pId null"), exerciseId = eId)
        )
      }
    }

    val expected = RoutineWithExerciseProgresses(
      routine = routine, exerciseProgresses = progresses.sortedBy { it.exercise.name })
    val actual = fetchRoutineFlow(dao = routineDao, routineId = routine.id).first()
    assertEquals(expected, actual)
  }
}