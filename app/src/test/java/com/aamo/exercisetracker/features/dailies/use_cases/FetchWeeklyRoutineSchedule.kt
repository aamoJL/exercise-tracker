package com.aamo.exercisetracker.features.dailies.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.ExerciseWithProgress
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithScheduleAndExerciseProgresses
import com.aamo.exercisetracker.features.dailies.DailiesScreenViewModel
import com.aamo.exercisetracker.utility.extensions.date.Day
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.LocalDate
import java.util.Calendar

@Suppress("HardCodedStringLiteral")
class FetchWeeklyRoutineScheduleTests {
  @Test
  fun `returns correct items`() = runBlocking {
    val currentTime = Calendar.getInstance().time
    val today = Day.TUESDAY
    val days = listOf(Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY)
    val routines = listOf(
      RoutineWithScheduleAndExerciseProgresses(
        routine = Routine(id = 0, name = "Routine"),
        schedule = RoutineSchedule(routineId = 0, tuesday = true, wednesday = true),
        exerciseProgresses = listOf(
          ExerciseWithProgress(
            exercise = Exercise(id = 0, routineId = 0), progress = ExerciseProgress(
              exerciseId = 0, finishedDate = currentTime
            )
          )
        )
      )
    )

    val result = fetchWeeklyRoutineScheduleFlow(
      getDataFlow = {
        flow { emit(routines) }
      }, currentDate = LocalDate.now(), weekDays = days, today = today
    ).first()

    assertEquals(result.size, 3)
    assert(result[0].isEmpty())
    assertEquals(
      result[1], listOf(
        DailiesScreenViewModel.RoutineModel(
          routine = routines[0].routine, progress = DailiesScreenViewModel.RoutineModel.Progress(
            finishedCount = 1, totalCount = 1
          )
        )
      )
    )
    assertEquals(
      result[2], listOf(
        DailiesScreenViewModel.RoutineModel(
          routine = routines[0].routine, progress = DailiesScreenViewModel.RoutineModel.Progress(
            finishedCount = 0, totalCount = 1
          )
        )
      )
    )
  }
}