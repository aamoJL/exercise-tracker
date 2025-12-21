package com.aamo.exercisetracker.features.exercise.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun SetInfo(set: ExerciseSet?, onStartSet: () -> Unit, onFinishExercise: () -> Unit) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    if (set != null) {
      UnfinishedSetInfo(
        set = set,
        hasTimer = set.valueType == ExerciseSet.ValueType.COUNTDOWN,
        onStartSet = onStartSet,
      )
    }
    else {
      FinishedSetInfo(onFinishExercise = onFinishExercise)
    }
  }
}

@Composable
private fun UnfinishedSetInfo(set: ExerciseSet, hasTimer: Boolean, onStartSet: () -> Unit) {
  Card {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .padding(24.dp)
        .fillMaxWidth()
    ) {
      Text(
        text = stringResource(R.string.title_current_set),
        style = MaterialTheme.typography.titleMedium
      )
      when (set.valueType) {
        ExerciseSet.ValueType.REPETITION -> RepetitionValueText(set.value, set.unit)
        ExerciseSet.ValueType.COUNTDOWN -> TimerValueText(set.value.milliseconds)
      }
    }
  }
  Button(
    shape = CardDefaults.shape,
    onClick = onStartSet,
    modifier = Modifier
      .heightIn(max = 100.dp)
      .fillMaxSize()
  ) {
    Text(
      text = ifElse(
        condition = hasTimer,
        ifTrue = { stringResource(R.string.btn_start) },
        ifFalse = { stringResource(R.string.btn_done) }),
      style = MaterialTheme.typography.titleLarge
    )
  }
}

@Composable
fun FinishedSetInfo(onFinishExercise: () -> Unit) {
  Button(
    shape = CardDefaults.shape,
    onClick = onFinishExercise,
    modifier = Modifier
      .heightIn(max = 100.dp)
      .fillMaxSize()
  ) {
    Text(stringResource(R.string.btn_done), style = MaterialTheme.typography.titleLarge)
  }
}

@Composable
private fun RepetitionValueText(value: Int, unit: String) {
  Text(text = "$value $unit", style = MaterialTheme.typography.displayMedium)
}

@Composable
private fun TimerValueText(value: Duration) {
  val hours = value.inWholeHours
  val minutes = value.inWholeMinutes % 60
  val seconds = value.inWholeSeconds % 60

  Text(text = StringBuilder().apply {
    if (hours > 0) append(stringResource(R.string.x_hours_short, hours))
    if (minutes > 0) append(stringResource(R.string.x_minutes_short, minutes))
    if (seconds > 0) append(stringResource(R.string.x_seconds_short, seconds))
  }.toString(), style = MaterialTheme.typography.displayMedium)
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun PreviewRepetitions() {
  SetInfo(
    set = ExerciseSet(
    id = 1L,
    exerciseId = 1L,
    value = 123,
    unit = "Reps",
    valueType = ExerciseSet.ValueType.REPETITION
  ), onStartSet = {}, onFinishExercise = {})
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun PreviewCountdown() {
  SetInfo(
    set = ExerciseSet(
    id = 1L,
    exerciseId = 1L,
    value = (1.hours + 2.minutes + 68.seconds).inWholeMilliseconds.toInt(),
    unit = "Reps",
    valueType = ExerciseSet.ValueType.COUNTDOWN
  ), onStartSet = {}, onFinishExercise = {})
}

@Preview
@Composable
private fun PreviewFinished() {
  SetInfo(set = null, onStartSet = {}, onFinishExercise = {})
}