package com.aamo.exercisetracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LoadingIconButton(
  onClick: () -> Unit,
  isLoading: Boolean,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  content: @Composable (() -> Unit),
) {
  Box(
    contentAlignment = Alignment.Center, modifier = modifier.minimumInteractiveComponentSize()
  ) {
    if (isLoading) {
      CircularProgressIndicator(
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )
    }
    IconButton(onClick = onClick, enabled = enabled, content = content)
  }
}