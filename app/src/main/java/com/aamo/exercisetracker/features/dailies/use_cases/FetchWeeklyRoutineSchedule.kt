package com.aamo.exercisetracker.features.dailies.use_cases

import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.features.dailies.DailiesScreenViewModel.RoutineModel
import com.aamo.exercisetracker.features.dailies.WeeklySchedule
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.toDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

fun fetchWeeklyRoutineScheduleFlow(
  currentDate: LocalDate,
  weekDays: List<Day>,
  getDataFlow: () -> Flow<Map<RoutineWithSchedule, List<Date?>>>,
): Flow<WeeklySchedule> {
  val todayIndex = weekDays.indexOf(currentDate.dayOfWeek.toDay())

  return getDataFlow().map { map ->
    weekDays.mapIndexed { i, day ->
      val dayMillis = currentDate.atStartOfDay().plusDays((i - todayIndex).toLong())
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

      map.filter { it.key.schedule.isDaySelected(day.getDayNumber()) }.map { item ->
        RoutineModel(
          routine = item.key.routine, progress = RoutineModel.Progress(
            finishedCount = item.value.count { date ->
              date?.time?.compareTo(dayMillis)?.let { it > 0 } == true
            }, totalCount = item.value.size
          )
        )
      }
    }
  }
}