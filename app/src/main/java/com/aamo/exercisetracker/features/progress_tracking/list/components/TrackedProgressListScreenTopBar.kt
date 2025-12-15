package com.aamo.exercisetracker.features.progress_tracking.list.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.ui.components.inputs.text_field.SearchTextField
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme

@Composable
fun TrackedProgressListScreenTopBar(
  selectionCount: Int,
  filterWord: String,
  onDeleteSelected: () -> Unit,
  onFilterChanged: (String) -> Unit,
  onAdd: () -> Unit
) {
  if (selectionCount > 0) {
    SelectionTopBar(selectionCount = selectionCount, onDeleteSelected = onDeleteSelected)
  }
  else {
    UnselectionTopBar(filterWord = filterWord, onFilterChanged = onFilterChanged, onAdd = onAdd)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnselectionTopBar(
  filterWord: String,
  onFilterChanged: (String) -> Unit,
  onAdd: () -> Unit,
) {
  TopAppBar(title = { }, actions = {
    SearchTextField(
      value = filterWord,
      placeholder = stringResource(R.string.ph_search),
      onValueChange = onFilterChanged
    )
    IconButton(onClick = onAdd) {
      Icon(
        painter = painterResource(R.drawable.rounded_add_24),
        contentDescription = stringResource(R.string.cd_add_tracked_progress)
      )
    }
  })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(selectionCount: Int, onDeleteSelected: () -> Unit) {
  TopAppBar(title = {
    Text(text = stringResource(R.string.x_count_selected, selectionCount))
  }, actions = {
    IconButton(onClick = onDeleteSelected) {
      Icon(
        painter = painterResource(R.drawable.rounded_delete_24),
        contentDescription = stringResource(R.string.cd_delete_tracked_progress)
      )
    }
  })
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun SelectedPreview() {
  ExerciseTrackerTheme {
    TrackedProgressListScreenTopBar(
      selectionCount = 2,
      filterWord = "Word",
      onDeleteSelected = {},
      onFilterChanged = {},
      onAdd = {})
  }
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun UnselectedPreview() {
  ExerciseTrackerTheme {
    TrackedProgressListScreenTopBar(
      selectionCount = 0,
      filterWord = "Word",
      onDeleteSelected = {},
      onFilterChanged = {},
      onAdd = {})
  }
}