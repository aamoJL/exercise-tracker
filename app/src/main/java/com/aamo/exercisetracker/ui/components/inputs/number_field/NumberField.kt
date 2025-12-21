package com.aamo.exercisetracker.ui.components.inputs.number_field

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import com.aamo.exercisetracker.utility.extensions.general.letIf

@Composable
fun <T : Any?> NumberField(
  value: T,
  onValueChange: (T) -> Unit,
  validator: FieldValidator<T>,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  textStyle: TextStyle = LocalTextStyle.current,
  label: @Composable (() -> Unit)? = null,
  placeholder: @Composable (() -> Unit)? = null,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  prefix: @Composable (() -> Unit)? = null,
  suffix: @Composable (() -> Unit)? = null,
  supportingText: @Composable (() -> Unit)? = null,
  isError: Boolean = false,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  interactionSource: MutableInteractionSource? = null,
  shape: Shape = TextFieldDefaults.shape,
  colors: TextFieldColors = TextFieldDefaults.colors(),
  /** Keeps selection on the right side of zero if the text is zero */
  restrictSelectionOnZero: Boolean = true,
) {
  var currentText by rememberSaveable { mutableStateOf("0") }
  var currentSelection by remember { mutableStateOf(TextRange(currentText.length)) }
  var error by remember { mutableStateOf<Error?>(null) }

  LaunchedEffect(value) {
    // Change text when value changes
    if (!validator.onValid(value = value) { valueText ->
        // ... but only if the currentText's value is different
        validator.onValid(text = currentText) { currentTextValue, _ ->
          if (value != currentTextValue) {
            currentText = valueText
            currentSelection = TextRange(currentText.length)
          }
        }
      }) {
      error = Error(value.toString())
    }
  }

  TextField(
    value = TextFieldValue(text = error?.message ?: currentText, selection = currentSelection),
    shape = shape,
    colors = colors,
    placeholder = placeholder,
    onValueChange = {
      if (currentText == it.text) {
        // Only selection changed
        // Prevent cursor movement if zero was prepended to the value
        currentSelection = it.selection.letIf(it.text == "0" && restrictSelectionOnZero) {
          TextRange(1)
        }
      }
      else {
        validator.onValid(text = it.text) { v, t ->
          if (currentText != t) {
            currentText = t
            currentSelection = it.selection.letIf(currentText == "0") { TextRange(1) }
          }
          if (value != v) onValueChange(v)
        }
      }
    },
    suffix = suffix,
    keyboardOptions = keyboardOptions.copy(keyboardType = KeyboardType.Number),
    enabled = enabled,
    readOnly = if (error == null) readOnly else true,
    textStyle = textStyle,
    label = label,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    prefix = prefix,
    supportingText = supportingText,
    isError = isError,
    visualTransformation = visualTransformation,
    keyboardActions = keyboardActions,
    maxLines = 1,
    minLines = 1,
    singleLine = true,
    interactionSource = interactionSource,
    modifier = modifier
  )
}

interface FieldValidator<T : Any?> {
  fun onValid(text: String, onValid: (value: T, text: String) -> Unit)
  fun onValid(value: T, onValid: (text: String) -> Unit): Boolean
}