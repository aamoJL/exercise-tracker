package com.aamo.exercisetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.aamo.exercisetracker.utility.tags.UITag

@Composable
fun LoadingScreen(
  loading: Boolean,
  modifier: Modifier = Modifier,
  indicatorAlignment: Alignment = Alignment.Center,
  content: @Composable () -> Unit = {}
) {
  if (loading) {
    Box(
      contentAlignment = indicatorAlignment,
      modifier = modifier
        .fillMaxSize()
        .background(color = Color.Transparent)
    ) {
      CircularProgressIndicator(modifier = Modifier.testTag(UITag.PROGRESS_INDICATOR.name))
    }
  }
  else {
    content()
  }
}