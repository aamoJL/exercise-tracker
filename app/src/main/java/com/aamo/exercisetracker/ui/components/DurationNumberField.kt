package com.aamo.exercisetracker.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.utility.extensions.date.DurationSegments
import com.aamo.exercisetracker.utility.extensions.form.HideZero
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * @param limitBiggestUnit Limits the biggest enabled unit so it will not go over its intended range.
 * For example: hours field would be limited to 23 hours
 * @param hideZeroOnDisabled Will show only the input labels if the value equals zero and the input has been disabled
 */
@Composable
fun DurationNumberField(
  value: Duration,
  onValueChange: (Duration) -> Unit,
  enabled: Boolean = true,
  hours: Boolean = true,
  minutes: Boolean = true,
  seconds: Boolean = true,
  limitBiggestUnit: Boolean = false,
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
  val segments = remember(value) { getDurationSegments(value, hours, minutes, seconds) }
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
    if (hours) {
      IntNumberField(
        enabled = enabled,
        value = segments.hours,
        onValueChange = {
          if (!limitBiggestUnit || it < 24) {
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
      if (minutes xor seconds) {
        divider()
      }
    }
    if (minutes) {
      IntNumberField(
        enabled = enabled,
        value = segments.minutes,
        onValueChange = {
          if (!hours && !limitBiggestUnit || it < 60) {
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
      if (seconds) {
        divider()
      }
    }
    if (seconds) {
      IntNumberField(
        enabled = enabled,
        value = segments.seconds,
        onValueChange = {
          if (!hours && !minutes && !limitBiggestUnit || it < 60) {
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