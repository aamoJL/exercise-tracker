package com.aamo.exercisetracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aamo.exercisetracker.R

@Composable
fun BackNavigationIconButton(onBack: () -> Unit) {
  IconButton(onClick = onBack) {
    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowBack,
      contentDescription = stringResource(R.string.cd_navigate_back)
    )
  }
}