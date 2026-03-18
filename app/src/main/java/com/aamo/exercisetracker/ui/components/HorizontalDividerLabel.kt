package com.aamo.exercisetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme

@Composable
fun HorizontalDividerLabel(
  label: String,
  modifier: Modifier = Modifier,
  color: Color = MaterialTheme.colorScheme.outline,
  style: TextStyle = MaterialTheme.typography.labelMedium,
  fontFamily: FontFamily? = null,
  minLineWidth: Dp = Dp.Unspecified,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier
  ) {
    HorizontalDivider(
      modifier = Modifier
        .weight(1f)
        .widthIn(min = minLineWidth), color = color.copy(alpha = .7f)
    )
    Text(
      text = label,
      color = color,
      style = style,
      fontFamily = fontFamily,
      modifier = Modifier.padding(horizontal = 2.dp)
    )
    HorizontalDivider(
      modifier = Modifier
        .weight(1f)
        .widthIn(min = minLineWidth), color = color.copy(alpha = .7f)
    )
  }
}

@Suppress("HardCodedStringLiteral")
@PreviewLightDark
@Composable
private fun Preview() {
  ExerciseTrackerTheme {
    Surface {
      HorizontalDividerLabel(label = "Label")
    }
  }
}