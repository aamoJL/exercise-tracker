package com.aamo.exercisetracker.ui.components.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun HorizontalRadioButton(
  title: String, selected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier
      .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
      .height(56.dp)
      .padding(horizontal = 8.dp)
  ) {
    RadioButton(selected = selected, onClick = null)
    Text(text = title)
  }
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun Preview() {
  HorizontalRadioButton(title = "Title", selected = true, onSelect = {})
}