package com.aamo.exercisetracker.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class BooleanPreviewParameterProvider(
  override val values: Sequence<Boolean> = sequenceOf(false, true)
) : PreviewParameterProvider<Boolean>