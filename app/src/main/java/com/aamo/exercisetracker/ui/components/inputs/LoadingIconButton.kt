package com.aamo.exercisetracker.ui.components.inputs

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.aamo.exercisetracker.utility.tags.UITag

@Composable
fun LoadingIconButton(
  onClick: () -> Unit,
  isLoading: Boolean,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  content: @Composable (() -> Unit),
) {
  Box(contentAlignment = Alignment.Center, modifier = modifier.minimumInteractiveComponentSize()) {
    if (isLoading) {
      CircularProgressIndicator(
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.testTag(UITag.PROGRESS_INDICATOR.name)
      )
    }
    IconButton(onClick = onClick, enabled = (enabled && !isLoading), content = content)
  }
}