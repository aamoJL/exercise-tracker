package com.aamo.exercisetracker.features.routine.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOrder
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import java.util.Calendar

@Composable
fun ScheduleTrailing(schedule: RoutineSchedule, color: Color = LocalContentColor.current) {
  fun dayIsSelected(day: Day): Boolean {
    return when (day) {
      Day.SUNDAY -> schedule.sunday
      Day.MONDAY -> schedule.monday
      Day.TUESDAY -> schedule.tuesday
      Day.WEDNESDAY -> schedule.wednesday
      Day.THURSDAY -> schedule.thursday
      Day.FRIDAY -> schedule.friday
      Day.SATURDAY -> schedule.saturday
    }
  }

  val colorLight = color.copy(alpha = .9f)
  val colorDim = colorLight.copy(alpha = .5f)
  val dayOrder = Calendar.getInstance().getLocalDayOrder()

  if (Day.entries.none { dayIsSelected(it) }) {
    Text(
      text = stringResource(R.string.label_inactive),
      color = colorDim,
      style = MaterialTheme.typography.labelSmall
    )
  }
  else if (Day.entries.all { dayIsSelected(it) }) {
    Text(
      text = stringResource(R.string.label_every_day),
      color = colorLight,
      style = MaterialTheme.typography.labelSmall
    )
  }
  else {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      repeat(7) { i ->
        Text(
          text = stringResource(dayOrder[i].nameResourceKey).take(2),
          color = ifElse(
            condition = dayIsSelected(day = dayOrder[i]),
            ifTrue = { colorLight },
            ifFalse = { colorDim }),
          style = MaterialTheme.typography.labelSmall
        )
      }
    }
  }
}

@PreviewLightDark
@Composable
private fun Preview(
  @PreviewParameter(ScheduleProvider::class) schedule: RoutineSchedule
) {
  ExerciseTrackerTheme {
    Surface(modifier = Modifier.padding(8.dp)) {
      ScheduleTrailing(schedule = schedule)
    }
  }
}

private class ScheduleProvider(
  override val values: Sequence<RoutineSchedule> = sequenceOf(
    RoutineSchedule(routineId = 1L),
    RoutineSchedule(routineId = 1L, wednesday = true, saturday = true),
    RoutineSchedule(
      routineId = 1L,
      sunday = true,
      monday = true,
      tuesday = true,
      wednesday = true,
      thursday = true,
      friday = true,
      saturday = true
    ),
  )
) : PreviewParameterProvider<RoutineSchedule>