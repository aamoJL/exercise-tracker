package com.aamo.exercisetracker.tests.features.dailies.use_cases

import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseProgress
import com.aamo.exercisetracker.database.entities.ExerciseWithProgress
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.features.dailies.DailiesScreenViewModel
import com.aamo.exercisetracker.features.dailies.use_cases.fetchWeeklyRoutineScheduleFlow
import com.aamo.exercisetracker.utility.extensions.date.Day
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.LocalDate
import java.util.Calendar

@Suppress("HardCodedStringLiteral")
class FetchWeeklyRoutineSchedule {
  @Test
  fun `returns correct items`() {
    val currentTime = Calendar.getInstance().apply {
      set(Calendar.YEAR, 2025)
      set(Calendar.MONTH, Calendar.OCTOBER)
      set(Calendar.DAY_OF_MONTH, 21)
    }.time // Tuesday
    val days = listOf(Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY)
    val routines = mapOf(
      RoutineWithSchedule(
        routine = Routine(id = 0, name = "Routine"),
        schedule = RoutineSchedule(routineId = 0, tuesday = true, wednesday = true)
      ) to listOf(
        ExerciseWithProgress(
          exercise = Exercise(routineId = 0L), progress = ExerciseProgress(
            exerciseId = 0L, finishedDate = currentTime
          )
        )
      )
    )

    val result = runBlocking {
      fetchWeeklyRoutineScheduleFlow(
        getDataFlow = {
          flow { emit(routines) }
        }, currentDate = LocalDate.of(2025, 10, 21), weekDays = days
      ).first()
    }

    assertEquals(result.size, 3)
    assert(result[0].isEmpty())
    assertEquals(
      listOf(
        DailiesScreenViewModel.RoutineModel(
          routine = routines.entries.elementAt(0).key.routine,
          progress = DailiesScreenViewModel.RoutineModel.Progress(
            finishedCount = 1, totalCount = 1
          )
        )
      ), result[1]
    )
    assertEquals(
      listOf(
        DailiesScreenViewModel.RoutineModel(
          routine = routines.entries.elementAt(0).key.routine,
          progress = DailiesScreenViewModel.RoutineModel.Progress(
            finishedCount = 0, totalCount = 1
          )
        )
      ), result[2]
    )
  }
}