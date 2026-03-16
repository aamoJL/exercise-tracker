package com.aamo.exercisetracker.features.dailies.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.ui.components.modals.CustomModalBottomSheet
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme

@Composable
fun UnfinishedTrackedProgressesModalBottomSheet(
  show: Boolean,
  trackedProgresses: List<TrackedProgress>,
  onDismissRequest: () -> Unit,
  onTrackedProgressSelected: (id: Long) -> Unit,
) {
  CustomModalBottomSheet(show = show, onDismissRequest = onDismissRequest) {
    ModalContent(
      trackedProgresses = trackedProgresses, onTrackedProgressSelected = onTrackedProgressSelected
    )
  }
}

@Composable
private fun ModalContent(
  trackedProgresses: List<TrackedProgress>,
  onTrackedProgressSelected: (id: Long) -> Unit,
) {
  Column {
    Text(
      text = stringResource(R.string.title_scheduled_trackers),
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.headlineSmall,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 16.dp)
    )
    LazyColumn(
      verticalArrangement = Arrangement.Top, modifier = Modifier.padding(horizontal = 32.dp)
    ) {
      items(items = trackedProgresses, key = { it.id }) { progress ->
        OutlinedButton(
          onClick = { onTrackedProgressSelected(progress.id) },
          shape = MaterialTheme.shapes.extraSmall,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(text = progress.name, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Suppress("HardCodedStringLiteral")
@PreviewLightDark
@Composable
private fun Preview() {
  ExerciseTrackerTheme {
    Surface(tonalElevation = 1.dp) {
      ModalContent(
        trackedProgresses = listOf(
          TrackedProgress(id = 1L, name = "Progress 1"),
          TrackedProgress(id = 2L, name = "Progress 2"),
          TrackedProgress(id = 3L, name = "Progress 3"),
        ),
        onTrackedProgressSelected = {},
      )
    }
  }
}