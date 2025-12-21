package com.aamo.exercisetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.aamo.exercisetracker.ui.preview.BooleanPreviewParameterProvider
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

@Composable
fun <T> LoadingScreen(
  model: T?,
  modifier: Modifier = Modifier,
  indicatorAlignment: Alignment = Alignment.Center,
  content: @Composable (T) -> Unit = {}
) {
  if (model == null) {
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
    content(model)
  }
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun PreviewLoading(
  @PreviewParameter(BooleanPreviewParameterProvider::class) loading: Boolean
) {
  LoadingScreen(loading = loading) {
    Text("Content")
  }
}

@Preview
@Composable
private fun PreviewModelLoading(
  @PreviewParameter(BooleanPreviewParameterProvider::class) loading: Boolean
) {
  LoadingScreen(model = loading.let { if (it) null else false }) {
    Text(it.toString())
  }
}