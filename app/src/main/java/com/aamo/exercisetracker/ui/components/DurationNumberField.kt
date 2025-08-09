package com.aamo.exercisetracker.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.aamo.exercisetracker.utility.extensions.date.DurationSegments
import com.aamo.exercisetracker.utility.extensions.form.HideZero
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class DurationNumberFieldFields(
  val hours: Properties = Properties(max = 23),
  val minutes: Properties = Properties(max = 59),
  val seconds: Properties = Properties(max = 59),
) {
  data class Properties(
    val enabled: Boolean = true, val max: Long = Long.MAX_VALUE
  )
}

/**
 * @param hideZeroOnDisabled Will show only the input labels if the value equals zero and the input has been disabled
 */
@Composable
fun DurationNumberField(
  value: Duration,
  onValueChange: (Duration) -> Unit,
  enabled: Boolean = true,
  fields: DurationNumberFieldFields = DurationNumberFieldFields(),
  hideZeroOnDisabled: Boolean = true,
  modifier: Modifier = Modifier,
  shape: Shape = TextFieldDefaults.shape,
  colors: TextFieldColors = TextFieldDefaults.colors(),
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  divider: @Composable (() -> Unit) = {
    Text(
      text = ":",
      textAlign = TextAlign.Center,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 20.sp,
      modifier = Modifier.padding(horizontal = 6.dp),
    )
  }
) {
  val (hours, minutes, seconds) = fields
  val segments =
    remember(value) { getDurationSegments(value, hours.enabled, minutes.enabled, seconds.enabled) }
  val visualTransformation = if (hideZeroOnDisabled && value == Duration.ZERO && !enabled) {
    VisualTransformation.HideZero
  }
  else {
    object : VisualTransformation {
      override fun filter(text: AnnotatedString): TransformedText {
        val value = text.text.toInt()
        val formatted = when (value) {
          0 -> "00"
          in 1..9 -> "0$value"
          else -> text.text
        }

        return TransformedText(
          text = AnnotatedString(formatted), offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
              return if (value < 10) offset + 1 else offset
            }

            override fun transformedToOriginal(offset: Int): Int {
              return if (value < 10) max(offset - 1, 0) else offset
            }
          })
      }
    }
  }

  Row(
    verticalAlignment = Alignment.CenterVertically, modifier = modifier
  ) {
    if (hours.enabled) {
      IntNumberField(
        enabled = enabled,
        value = segments.hours,
        onValueChange = {
          if (it <= hours.max || it < 24) {
            onValueChange(segments.copy(hours = it).toDuration())
          }
        },
        shape = shape,
        label = {
          Text(
            text = stringResource(R.string.label_hours),
            overflow = TextOverflow.Ellipsis,
            softWrap = false
          )
        },
        visualTransformation = visualTransformation,
        colors = colors,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        modifier = Modifier.weight(1f)
      )
      if (minutes.enabled || seconds.enabled) {
        divider()
      }
    }
    if (minutes.enabled) {
      IntNumberField(
        enabled = enabled,
        value = segments.minutes,
        onValueChange = {
          if (it <= minutes.max || it < 60) {
            onValueChange(segments.copy(minutes = it).toDuration())
          }
        },
        shape = shape,
        label = {
          Text(
            text = stringResource(R.string.label_minutes),
            overflow = TextOverflow.Ellipsis,
            softWrap = false
          )
        },
        visualTransformation = visualTransformation,
        colors = colors,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        modifier = Modifier.weight(1f)
      )
      if (seconds.enabled) {
        divider()
      }
    }
    if (seconds.enabled) {
      IntNumberField(
        enabled = enabled,
        value = segments.seconds,
        onValueChange = {
          if (it <= seconds.max || it < 60) {
            onValueChange(segments.copy(seconds = it).toDuration())
          }
        },
        shape = shape,
        label = {
          Text(
            text = stringResource(R.string.label_seconds),
            overflow = TextOverflow.Ellipsis,
            softWrap = false
          )
        },
        visualTransformation = visualTransformation,
        colors = colors,
        keyboardOptions = keyboardOptions,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

private fun getDurationSegments(
  value: Duration,
  hours: Boolean,
  minutes: Boolean,
  seconds: Boolean,
): DurationSegments {
  val hours = if (hours) value.inWholeHours else 0L
  val minutes = if (minutes) (value - hours.hours).inWholeMinutes else 0L
  val seconds = if (seconds) (value - hours.hours - minutes.minutes).inWholeSeconds else 0L

  return DurationSegments(
    seconds = seconds.toInt(), minutes = minutes.toInt(), hours = hours.toInt()
  )
}

@Preview
@Composable
private fun Preview() {
  ExerciseTrackerTheme {
    Surface {
      DurationNumberField(
        value = (10.hours + 46.minutes + 55.seconds), onValueChange = {})
    }
  }
}