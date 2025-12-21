package com.aamo.exercisetracker.features.exercise.form.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
  imeAction: ImeAction = ImeAction.Default,
) {
  val density = LocalDensity.current
  val dismissState = rememberSwipeToDismissBoxState(
    positionalThreshold = { with(density) { 70.dp.toPx() } })
  val deleteDirection = SwipeToDismissBoxValue.StartToEnd

  LaunchedEffect(dismissState.currentValue) {
    if (dismissState.currentValue == deleteDirection) {
      onRemove()
    }
  }

  SwipeToDismissBox(
    state = dismissState,
    enableDismissFromStartToEnd = true,
    enableDismissFromEndToStart = false,
    backgroundContent = {
      BackgroundContent(state = dismissState, deleteDirection = deleteDirection)
    },
    modifier = modifier
  ) {
    if (isDuration) {
      Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()
      ) {
        DurationNumberField(
          value = value.milliseconds,
          onValueChange = { onChange(it.inWholeMilliseconds.toInt()) },
          fields = DurationNumberFieldFields(
            hours = DurationNumberFieldFields.Properties(enabled = false)
          ),
          shape = RectangleShape,
          colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline,
            focusedPlaceholderColor = MaterialTheme.colorScheme.outline
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
          unfocusedIndicatorColor = Color.Transparent,
          focusedIndicatorColor = Color.Transparent,
          unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline,
          focusedPlaceholderColor = MaterialTheme.colorScheme.outline
        ),
        placeholder = { Text(stringResource(R.string.ph_amount)) },
        suffix = { Text(suffix) },
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Composable
private fun BackgroundContent(
  state: SwipeToDismissBoxState, deleteDirection: SwipeToDismissBoxValue
) {
  val color = when (state.targetValue) {
    deleteDirection -> MaterialTheme.colorScheme.errorContainer
    else -> Color.Transparent
  }
  val icon = when (state.dismissDirection) {
    deleteDirection -> painterResource(R.drawable.rounded_delete_24)
    else -> null
  }
  val alignment = when (deleteDirection) {
    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
    else -> Alignment.CenterStart
  }

  Box(
    contentAlignment = alignment, modifier = Modifier
      .fillMaxSize()
      .background(color)
  ) {
    if (icon != null) {
      Icon(
        painter = icon,
        contentDescription = null,
        modifier = Modifier.minimumInteractiveComponentSize()
      )
    }
  }
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun PreviewCount() {
  ExerciseSetSwipeToDismissBox(
    value = 123,
    isDuration = false,
    suffix = "Reps",
    onChange = {},
    onRemove = {})
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun PreviewDuration() {
  ExerciseSetSwipeToDismissBox(
    value = (4.minutes + 25.seconds).inWholeMilliseconds.toInt(),
    isDuration = true,
    suffix = "Reps",
    onChange = {},
    onRemove = {})
}