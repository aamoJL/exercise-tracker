package com.aamo.exercisetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme

@Composable
fun FormList(
  title: String,
  modifier: Modifier = Modifier,
  actions: @Composable (RowScope.() -> Unit) = {},
  content: @Composable () -> Unit
) {
  ElevatedCard(shape = RoundedCornerShape(8.dp), modifier = modifier) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = modifier.fillMaxWidth()) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
          .padding(vertical = 8.dp, horizontal = 12.dp)
          .fillMaxWidth()
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleLarge,
        )
        Row(modifier = Modifier.padding(end = 4.dp)) {
          actions()
        }
      }
    }
    HorizontalDivider()
    content()
  }
}

@Suppress("HardCodedStringLiteral")
@PreviewLightDark
@Composable
private fun Preview() {
  ExerciseTrackerTheme() {
    FormList(title = "Title", actions = {
      OutlinedIconButton(onClick = {}) {
        Icon(
          painter = painterResource(R.drawable.rounded_add_24),
          contentDescription = null,
        )
      }
    }) {
      Text(text = "Content")
    }
  }
}