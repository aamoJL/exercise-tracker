package com.aamo.exercisetracker.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aamo.exercisetracker.R

@Composable
fun UnsavedDialog(
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) {
  AlertDialog(
    title = { Text(text = stringResource(R.string.dialog_title_unsaved_changes)) },
    text = { Text(text = stringResource(R.string.dialog_text_unsaved_changes)) },
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onConfirm, colors = ButtonDefaults.textButtonColors()) {
        Text(stringResource(R.string.btn_yes))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.btn_cancel))
      }
    })
}