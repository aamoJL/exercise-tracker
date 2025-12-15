package com.aamo.exercisetracker.features.progress_tracking.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.ui.components.modals.GesturelessModalBottomSheet
import com.aamo.exercisetracker.utility.extensions.date.toClockString
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import kotlin.concurrent.timer
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchSheet(
  show: Boolean,
  active: Boolean,
  title: String,
  // Needs to be here because back button will request dismiss
  onDismissRequest: () -> Unit,
  onStart: () -> Unit,
  onStop: () -> Unit,
  onCancel: () -> Unit,
) {
  val startTime = rememberSaveable(active) { System.currentTimeMillis() }
  var clockText by rememberSaveable(active) {
    mutableStateOf((System.currentTimeMillis() - startTime).milliseconds.toClockString())
  }

  DisposableEffect(active) {
    // Update clock text
    val timer = if (active) timer(period = 1.seconds.inWholeMilliseconds) {
      clockText = (System.currentTimeMillis() - startTime).milliseconds.toClockString()
    }
    else null

    onDispose {
      timer?.cancel()
      timer?.purge()
    }
  }

  GesturelessModalBottomSheet(show = show, onDismissRequest = onDismissRequest) {
    Box(
      contentAlignment = Alignment.BottomCenter, modifier = Modifier
        .fillMaxWidth(.7f)
        .weight(3f)
    ) {
      Box(
        contentAlignment = Alignment.Center, modifier = Modifier.aspectRatio(1f)
      ) {
        CircularProgressIndicator(
          progress = { if (active) 1f else 0f },
          strokeWidth = 20.dp,
          gapSize = 0.dp,
          strokeCap = StrokeCap.Butt,
          modifier = Modifier.fillMaxSize()
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(title, style = MaterialTheme.typography.titleMedium)
          Text(
            text = clockText,
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
          )
          Spacer(Modifier.height(with(LocalDensity.current) {
            // Centers the text inside the progress indicator
            MaterialTheme.typography.titleMedium.lineHeight.toDp()
          }))
        }
      }
    }
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceEvenly,
      modifier = Modifier
        .fillMaxWidth()
        .weight(2f)
        .padding(horizontal = 16.dp)
    ) {
      IconButton(
        onClick = onCancel, colors = IconButtonDefaults.iconButtonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError,
        ), modifier = Modifier.size(64.dp)
      ) {
        Icon(
          painter = painterResource(R.drawable.rounded_cancel_24),
          contentDescription = stringResource(R.string.btn_cancel),
          modifier = Modifier.size(48.dp)
        )
      }
      ifElse(condition = active, ifTrue = {
        // Stop button
        IconButton(
          onClick = onStop, colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = .38f),
            disabledContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = .38f),
          ), modifier = Modifier.size(64.dp)
        ) {
          Icon(
            painter = painterResource(R.drawable.rounded_stop_circle_24),
            contentDescription = stringResource(R.string.btn_stop),
            modifier = Modifier.size(48.dp)
          )
        }
      }, ifFalse = {
        // Start button
        IconButton(
          onClick = onStart, colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = .38f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = .38f),
          ), modifier = Modifier.size(64.dp)
        ) {
          Icon(
            painter = painterResource(R.drawable.rounded_play_circle_24),
            contentDescription = stringResource(R.string.btn_start),
            modifier = Modifier.size(48.dp)
          )
        }
      })
    }
  }
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun Preview(@PreviewParameter(StopwatchSheetActiveProvider::class) active: Boolean) {
  StopwatchSheet(
    show = true,
    active = active,
    title = "Title",
    onDismissRequest = {},
    onStart = {},
    onStop = {},
    onCancel = {})
}

class StopwatchSheetActiveProvider(
  override val values: Sequence<Boolean> = sequenceOf(false, true)
) : PreviewParameterProvider<Boolean>