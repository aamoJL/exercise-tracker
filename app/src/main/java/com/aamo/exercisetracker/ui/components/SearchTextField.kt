package com.aamo.exercisetracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aamo.exercisetracker.utility.extensions.string.EMPTY

@Composable
fun SearchTextField(
  value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    placeholder = { Text("Search...") },
    leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
    trailingIcon = {
      if (value.isNotEmpty()) {
        IconButton(onClick = { onValueChange(String.EMPTY) }) {
          Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear")
        }
      }
    },
    modifier = modifier
  )
}