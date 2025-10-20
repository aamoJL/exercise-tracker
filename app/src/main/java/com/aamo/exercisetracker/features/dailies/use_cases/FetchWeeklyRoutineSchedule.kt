package com.aamo.exercisetracker.features.dailies.use_cases

import com.aamo.exercisetracker.database.entities.RoutineWithScheduleAndExerciseProgresses
import com.aamo.exercisetracker.features.dailies.DailiesScreenViewModel.RoutineModel
import com.aamo.exercisetracker.features.dailies.WeeklySchedule
import com.aamo.exercisetracker.utility.extensions.date.Day
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

fun fetchWeeklyRoutineScheduleFlow(
  getDataFlow: () -> Flow<List<RoutineWithScheduleAndExerciseProgresses>>,
  currentDate: LocalDate,
  weekDays: List<Day>,
  today: Day
): Flow<WeeklySchedule> {
  val todayIndex = weekDays.indexOf(today)

  return getDataFlow().map { list ->
    weekDays.mapIndexed { i, day ->
      val dayMillis = currentDate.atStartOfDay().plusDays((i - todayIndex).toLong())
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

      list.filter { it.schedule?.isDaySelected(day.getDayNumber()) == true }.map { item ->
        RoutineModel(
          routine = item.routine, progress = RoutineModel.Progress(
            finishedCount = item.exerciseProgresses.count { (_, progress) ->
              progress?.finishedDate?.time?.compareTo(dayMillis)?.let { it > 0 } == true
            }, totalCount = item.exerciseProgresses.size
          )
        )
      }
    }
  }
}