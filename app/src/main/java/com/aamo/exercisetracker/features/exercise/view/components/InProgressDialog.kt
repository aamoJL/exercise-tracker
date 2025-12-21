package com.aamo.exercisetracker.features.exercise.view.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.aamo.exercisetracker.R

@Composable
fun InProgressDialog(
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) {
  AlertDialog(
    title = { Text(text = stringResource(R.string.dialog_title_exercise_in_progress)) },
    text = { Text(stringResource(R.string.dialog_text_exercise_in_progress)) },
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text(text = stringResource(R.string.btn_yes))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = stringResource(R.string.btn_cancel))
      }
    },
  )
}

@Preview
@Composable
private fun Preview() {
  InProgressDialog(onDismiss = {}, onConfirm = {})
}