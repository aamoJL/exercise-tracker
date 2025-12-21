package com.aamo.exercisetracker.features.routine.form.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOrder
import java.util.Calendar

@Composable
fun ScheduleInput(
  selections: List<Day>, onChange: (List<Day>) -> Unit, modifier: Modifier = Modifier
) {
  val days = remember { Calendar.getInstance().getLocalDayOrder() }

  Column(modifier = modifier) {
    LazyRow(
      horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()
    ) {
      items(days) { day ->
        val isSelected = selections.contains(day)

        IconToggleButton(
          colors = IconButtonDefaults.outlinedIconToggleButtonColors(
            checkedContainerColor = MaterialTheme.colorScheme.inversePrimary,
            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            contentColor = MaterialTheme.colorScheme.outline,
          ), checked = isSelected, onCheckedChange = { selection ->
            if (selection && !selections.contains(day)) onChange(selections.plus(day))
            else onChange(selections.minus(day))
          }) {
          Text(stringResource(day.nameResourceKey).take(2))
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  ScheduleInput(selections = listOf(Day.WEDNESDAY, Day.SATURDAY), onChange = {})
}