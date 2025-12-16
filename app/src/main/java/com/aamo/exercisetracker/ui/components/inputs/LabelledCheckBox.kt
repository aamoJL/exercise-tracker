package com.aamo.exercisetracker.ui.components.inputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
  label: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }

  Row(
    verticalAlignment = Alignment.CenterVertically, modifier = modifier
      .toggleable(
        interactionSource = interactionSource,
        indication = null,
        value = checked,
        role = Role.Checkbox,
        onValueChange = onCheckedChange
      )
      .padding(horizontal = 4.dp, vertical = 8.dp)
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = onCheckedChange,
      interactionSource = interactionSource,
      modifier = Modifier.minimumInteractiveComponentSize()
    )
    label()
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