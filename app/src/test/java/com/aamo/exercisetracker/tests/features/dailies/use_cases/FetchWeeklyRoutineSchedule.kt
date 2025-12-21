package com.aamo.exercisetracker.tests.features.dailies.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.features.dailies.models.DailiesRoutineModel
import com.aamo.exercisetracker.features.dailies.use_cases.fetchWeeklyRoutineScheduleFlow
import com.aamo.exercisetracker.test_utility.database.DatabaseTest
import com.aamo.exercisetracker.utility.extensions.date.Day
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.util.Calendar

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class FetchWeeklyRoutineSchedule : DatabaseTest() {
  @Test
  fun `returns correct items`() = runTest {
    val routine = Routine(name = "Routine").let {
      routineDao.upsert(it).let { id ->
        routineDao.upsert(RoutineSchedule(routineId = id, tuesday = true, wednesday = true))
        it.copy(id = id)
      }
    }

    Exercise(routineId = routine.id).also {
      routineDao.upsert(it).let { id ->
        routineDao.upsert(
          ExerciseProgress(
            exerciseId = id, finishedDate = Calendar.getInstance().apply {
              set(Calendar.YEAR, 2025)
              set(Calendar.MONTH, Calendar.OCTOBER)
              set(Calendar.DAY_OF_MONTH, 21)
            }.time // Tuesday
          )
        )
        it.copy(id = id)
      }
    }

    val result = fetchWeeklyRoutineScheduleFlow(
      dao = routineDao,
      currentDate = LocalDate.of(2025, 10, 21),
      weekDays = listOf(Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY)
    ).first()

    assertEquals(result.size, 3)
    assert(result[0].isEmpty())

    assertEquals(
      listOf(
        DailiesRoutineModel(
          routine = routine,
          progress = DailiesRoutineModel.Progress(finishedCount = 1, totalCount = 1)
        )
      ), result[1]
    )
    assertEquals(
      listOf(
        DailiesRoutineModel(
          routine = routine,
          progress = DailiesRoutineModel.Progress(finishedCount = 0, totalCount = 1)
        )
      ), result[2]
    )
  }
}