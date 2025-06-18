package com.aamo.exercisetracker.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun UnsavedDialog(
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) {
  AlertDialog(
    title = { Text(text = "Unsaved changes") },
    text = { Text(text = "Go back without saving?") },
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onConfirm, colors = ButtonDefaults.textButtonColors()) {
        Text("Yes")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    })
}