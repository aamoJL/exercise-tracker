package com.aamo.exercisetracker.features.exercise.view.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.ui.components.modals.GesturelessModalBottomSheet
import com.aamo.exercisetracker.utility.extensions.date.toClockString
import kotlin.concurrent.timer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun SetTimerSheet(
  show: Boolean,
  duration: Duration,
  onDismissRequest: () -> Unit,
  onStopSetTimer: () -> Unit,
  onCancelSet: () -> Unit,
) {
  TimerSheet(
    show = show,
    title = stringResource(R.string.title_set_timer),
    active = show,
    duration = duration,
    onDismissRequest = onDismissRequest
  ) {
    IconButton(
      onClick = onCancelSet, colors = IconButtonDefaults.iconButtonColors(
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
    IconButton(
      onClick = onStopSetTimer, colors = IconButtonDefaults.iconButtonColors(
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
  }
}

@Composable
fun RestTimerSheet(
  show: Boolean, duration: Duration, onDismissRequest: () -> Unit, onStopRest: () -> Unit
) {
  TimerSheet(
    show = show,
    title = stringResource(R.string.title_rest),
    active = show,
    duration = duration,
    onDismissRequest = onDismissRequest
  ) {
    IconButton(
      onClick = onStopRest, colors = IconButtonDefaults.iconButtonColors(
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
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimerSheet(
  show: Boolean,
  title: String,
  active: Boolean,
  duration: Duration,
  onDismissRequest: () -> Unit, // Needs to be here because back button will request dismiss
  content: @Composable RowScope.() -> Unit,
) {
  var startTime by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
  var remainingMillis by rememberSaveable { mutableLongStateOf(duration.inWholeMilliseconds) }
  var animatableProgress by remember { mutableStateOf(Animatable(initialValue = 0f)) }

  LaunchedEffect(show) {
    if (show) {
      remainingMillis = duration.inWholeMilliseconds
      animatableProgress = Animatable(initialValue = 0f)
    }
  }

  LaunchedEffect(active) {
    if (active) {
      // Animate progress indicator
      animatableProgress = Animatable(initialValue = 0f)
      animatableProgress.animateTo(
        targetValue = 1f, animationSpec = tween(
          durationMillis = duration.inWholeMilliseconds.toInt(), easing = LinearEasing
        )
      )
    }
    else animatableProgress.stop()
  }

  DisposableEffect(active, show) {
    val timer = if (active && show) {
      startTime = System.currentTimeMillis()

      timer(period = 1.seconds.inWholeMilliseconds) {
        // Update clock text
        remainingMillis = duration.inWholeMilliseconds - (System.currentTimeMillis() - startTime)
      }
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
          progress = { animatableProgress.value },
          strokeWidth = 20.dp,
          gapSize = 0.dp,
          strokeCap = StrokeCap.Butt,
          modifier = Modifier.fillMaxSize()
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(title, style = MaterialTheme.typography.titleMedium)
          Text(
            text = remainingMillis.milliseconds.toClockString(),
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
      content()
    }
  }
}

@Preview
@Composable
private fun RestTimerSheetPreview(
) {
  RestTimerSheet(show = true, duration = 3.minutes, onDismissRequest = {}, onStopRest = {})
}

@Preview
@Composable
private fun SetTimerSheetPreview(
) {
  SetTimerSheet(
    show = true,
    duration = 3.minutes,
    onDismissRequest = {},
    onStopSetTimer = {},
    onCancelSet = {})
}