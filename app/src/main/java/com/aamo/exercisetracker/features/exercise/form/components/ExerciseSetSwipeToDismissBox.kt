package com.aamo.exercisetracker.features.exercise.form.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.ui.components.inputs.number_field.DurationNumberField
import com.aamo.exercisetracker.ui.components.inputs.number_field.DurationNumberFieldFields
import com.aamo.exercisetracker.ui.components.inputs.number_field.IntFieldValidator
import com.aamo.exercisetracker.ui.components.inputs.number_field.NumberField
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun ExerciseSetSwipeToDismissBox(
  value: Int,
  isDuration: Boolean,
  suffix: String,
  onChange: (Int) -> Unit,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier,
  color: Color = MaterialTheme.colorScheme.surfaceContainer,
  imeAction: ImeAction = ImeAction.Default,
) {
  BasicDismissibleItem(dismissAction = onRemove, modifier = modifier) {
    if (isDuration) {
      Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
      ) {
        DurationNumberField(
          value = value.milliseconds,
          onValueChange = { onChange(it.inWholeMilliseconds.toInt()) },
          fields = DurationNumberFieldFields(
            hours = DurationNumberFieldFields.Properties(enabled = false)
          ),
          shape = RectangleShape,
          colors = TextFieldDefaults.colors(
            focusedContainerColor = color,
            unfocusedContainerColor = color,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
          ),
          lastImeAction = imeAction,
          modifier = Modifier.fillMaxWidth(.5f)
        )
      }
    }
    else {
      NumberField(
        value = value,
        onValueChange = onChange,
        validator = IntFieldValidator,
        shape = RectangleShape,
        colors = TextFieldDefaults.colors(
          focusedContainerColor = color,
          unfocusedContainerColor = color,
          unfocusedIndicatorColor = Color.Transparent,
          focusedIndicatorColor = Color.Transparent,
        ),
        placeholder = { Text(stringResource(R.string.ph_amount)) },
        suffix = { Text(suffix) },
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicDismissibleItem(
  dismissAction: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable (RowScope.() -> Unit),
) {
  val positionalThreshold = with(LocalDensity.current) { 150.dp.toPx() }
  val dismissState = rememberSwipeToDismissBoxState(positionalThreshold = { positionalThreshold })

  SwipeToDismissBox(
    state = dismissState,
    backgroundContent = { DismissBackground(dismissState) },
    enableDismissFromEndToStart = false,
    enableDismissFromStartToEnd = true,
    onDismiss = { dir -> if (dir == SwipeToDismissBoxValue.StartToEnd) dismissAction() },
    content = content,
    modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissBackground(dismissState: SwipeToDismissBoxState) {
  val color = when (dismissState.dismissDirection) {
    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.errorContainer
    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.primaryContainer
    else -> Color.Transparent
  }

  val direction = dismissState.dismissDirection

  Row(
    modifier = Modifier
      .fillMaxSize()
      .background(color)
      .padding(12.dp, 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    if (direction == SwipeToDismissBoxValue.StartToEnd) Icon(
      painter = painterResource(R.drawable.rounded_delete_24), contentDescription = null
    )
  }
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun PreviewCount() {
  ExerciseTrackerTheme {
    Surface {
      ExerciseSetSwipeToDismissBox(
        value = 123,
        isDuration = false,
        suffix = "Reps",
        onChange = {},
        onRemove = {})
    }
  }
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun PreviewDuration() {
  ExerciseTrackerTheme {
    ExerciseSetSwipeToDismissBox(
      value = (4.minutes + 25.seconds).inWholeMilliseconds.toInt(),
      isDuration = true,
      suffix = "Reps",
      onChange = {},
      onRemove = {})
  }
}