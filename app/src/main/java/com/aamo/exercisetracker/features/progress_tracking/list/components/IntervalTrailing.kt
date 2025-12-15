package com.aamo.exercisetracker.features.progress_tracking.list.components

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
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme

@Composable
fun IntervalTrailing(intervalWeeks: Int, color: Color = LocalContentColor.current) {
  val colorLight = color.copy(alpha = .9f)
  val colorDim = color.copy(alpha = .5f)

  val text = when (intervalWeeks) {
    0 -> stringResource(R.string.label_untimed)
    1 -> stringResource(R.string.label_weekly)
    else -> stringResource(R.string.label_every_x_weeks, intervalWeeks)
  }

  Text(
    text = text, color = when (intervalWeeks) {
      0 -> colorDim
      else -> colorLight
    }, style = MaterialTheme.typography.labelSmall
  )
}

@PreviewLightDark
@Composable
private fun Preview(
  @PreviewParameter(IntervalWeeksProvider::class) intervalWeeks: Int
) {
  ExerciseTrackerTheme {
    Surface(modifier = Modifier.padding(8.dp)) {
      IntervalTrailing(intervalWeeks = intervalWeeks)
    }
  }
}

private class IntervalWeeksProvider(override val values: Sequence<Int> = sequenceOf(0, 1, 2)) :
        PreviewParameterProvider<Int>