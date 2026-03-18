package com.aamo.exercisetracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BackgroundSurface(modifier: Modifier = Modifier, content: @Composable (() -> Unit)) {
  Surface(color = MaterialTheme.colorScheme.background, modifier = modifier, content = content)
}