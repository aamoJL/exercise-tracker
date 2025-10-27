package com.aamo.exercisetracker.features.dailies.use_cases

import com.aamo.exercisetracker.database.entities.ExerciseWithProgress
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.features.dailies.DailiesScreenViewModel.RoutineModel
import com.aamo.exercisetracker.features.dailies.WeeklySchedule
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.toDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

fun fetchWeeklyRoutineScheduleFlow(
  currentDate: LocalDate,
  weekDays: List<Day>,
  getDataFlow: () -> Flow<Map<RoutineWithSchedule, List<ExerciseWithProgress>>>,
): Flow<WeeklySchedule> {
  val todayIndex = weekDays.indexOf(currentDate.dayOfWeek.toDay())

  return getDataFlow().map { map ->
    weekDays.mapIndexed { i, day ->
      val dayMillis = currentDate.atStartOfDay().plusDays((i - todayIndex).toLong())
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

      map.filter { (rws, _) -> rws.schedule?.isDaySelected(day.getDayNumber()) == true }
        .map { (rws, progresses) ->
          RoutineModel(
            routine = rws.routine, progress = RoutineModel.Progress(
              finishedCount = progresses.count { (_, progress) ->
                progress?.finishedDate?.time?.compareTo(dayMillis)?.let { it > 0 } == true
              }, totalCount = progresses.size
            )
          )
        }
    }
  }
}