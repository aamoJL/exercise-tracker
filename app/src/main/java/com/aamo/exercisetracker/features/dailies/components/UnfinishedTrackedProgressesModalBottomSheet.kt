package com.aamo.exercisetracker.features.dailies.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
    Text(
      text = stringResource(R.string.title_scheduled_trackers),
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.headlineSmall,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 16.dp)
    )
    LazyColumn(
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(bottom = 8.dp)
    ) {
      items(items = trackedProgresses, key = { it.id }) { progress ->
        Button(
          onClick = { onTrackedProgressSelected(progress.id) },
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(text = progress.name, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun Preview() {
  ExerciseTrackerTheme {
    UnfinishedTrackedProgressesModalBottomSheet(
      show = true, trackedProgresses = listOf(
      TrackedProgress(id = 1L, name = "Progress 1"),
      TrackedProgress(id = 2L, name = "Progress 2"),
      TrackedProgress(id = 3L, name = "Progress 3"),
    ), onDismissRequest = {}, onTrackedProgressSelected = {})
  }
}