package com.aamo.exercisetracker.ui.components.inputs

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LabelledCheckBox(
  checked: Boolean,
  onCheckedChange: ((Boolean) -> Unit),
  modifier: Modifier = Modifier,
  label: @Composable () -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }

  Row(
    verticalAlignment = Alignment.CenterVertically, modifier = modifier.toggleable(
      interactionSource = interactionSource,
      indication = LocalIndication.current,
      value = checked,
      role = Role.Checkbox,
      onValueChange = onCheckedChange,
    )
  ) {
    CompositionLocalProvider(LocalRippleConfiguration provides null) {
      Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        interactionSource = interactionSource,
      )
    }
    Box(modifier = Modifier.padding(start = 4.dp, end = 14.dp)) {
      label()
    }
  }
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun Preview() {
  LabelledCheckBox(
    checked = false,
    onCheckedChange = { },
    label = { Text("Label") },
  )
}