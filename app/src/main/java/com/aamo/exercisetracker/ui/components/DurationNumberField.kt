package com.aamo.exercisetracker.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.utility.extensions.date.DurationSegments
import kotlin.time.Duration

@Composable
fun DurationNumberField(
  value: Duration,
  onValueChange: (Duration) -> Unit,
  modifier: Modifier = Modifier,
  shape: Shape = TextFieldDefaults.shape,
  divider: @Composable (() -> Unit) = {
    Text(
      text = ":",
      textAlign = TextAlign.Center,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 20.sp,
      modifier = Modifier.padding(horizontal = 12.dp),
    )
  }
) {
  val segments = remember(value) {
    value.toComponents { h, m, s, _ ->
      {
        DurationSegments(seconds = s, minutes = m, hours = h.toInt())
      }
    }()
  }
  val visualTransformation = object : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
      val value = text.text.toInt()
      val formatted = if (value == 0) "00" else text.text

      return TransformedText(
        text = AnnotatedString(formatted), offsetMapping = object : OffsetMapping {
          override fun originalToTransformed(offset: Int): Int {
            return if (value == 0 && offset == 1) 2 else offset
          }

          override fun transformedToOriginal(offset: Int): Int {
            return if (value == 0 && (offset in (1..2))) 1 else offset
          }
        })
    }
  }

  Row(
    verticalAlignment = Alignment.CenterVertically, modifier = modifier
  ) {
    IntNumberField(
      value = segments.hours,
      onValueChange = { onValueChange(segments.copy(hours = it).toDuration()) },
      shape = shape,
      label = {
        Text(
          text = stringResource(R.string.label_hours),
          overflow = TextOverflow.Ellipsis,
          softWrap = false
        )
      },
      visualTransformation = visualTransformation,
      modifier = Modifier.weight(1f)
    )
    divider()
    IntNumberField(
      value = segments.minutes,
      onValueChange = { if (it < 60) onValueChange(segments.copy(minutes = it).toDuration()) },
      shape = shape,
      label = {
        Text(
          text = stringResource(R.string.label_minutes),
          overflow = TextOverflow.Ellipsis,
          softWrap = false
        )
      },
      visualTransformation = visualTransformation,
      modifier = Modifier.weight(1f)
    )
    divider()
    IntNumberField(
      value = segments.seconds,
      onValueChange = { if (it < 60) onValueChange(segments.copy(seconds = it).toDuration()) },
      shape = shape,
      label = {
        Text(
          text = stringResource(R.string.label_seconds),
          overflow = TextOverflow.Ellipsis,
          softWrap = false
        )
      },
      visualTransformation = visualTransformation,
      modifier = Modifier.weight(1f)
    )
  }
}