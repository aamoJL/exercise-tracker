package com.aamo.exercisetracker.ui.components.inputs.text_field

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.aamo.exercisetracker.utility.extensions.general.EMPTY

@Composable
fun SearchTextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = String.EMPTY,
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    placeholder = { Text(placeholder) },
    leadingIcon = {
      Icon(
        painter = painterResource(R.drawable.rounded_search_24), contentDescription = null
      )
    },
    trailingIcon = {
      if (value.isNotEmpty()) {
        IconButton(onClick = { onValueChange(String.EMPTY) }) {
          Icon(
            painter = painterResource(R.drawable.round_clear_24),
            contentDescription = stringResource(R.string.cd_clear)
          )
        }
      }
    },
    singleLine = true,
    modifier = modifier
  )
}

@Suppress("HardCodedStringLiteral")
@PreviewLightDark
@Composable
private fun Preview() {
  ExerciseTrackerTheme {
    Surface(modifier = Modifier.padding(20.dp)) {
      SearchTextField(
        value = "search text", placeholder = String.EMPTY, onValueChange = {})
    }
  }
}