package com.aamo.exercisetracker.features.progress_tracking.view.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.ui.components.modals.GesturelessModalBottomSheet
import com.aamo.exercisetracker.utility.extensions.date.toClockString
import kotlin.concurrent.timer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownSheet(
  show: Boolean,
  title: String,
  active: Boolean,
  duration: Duration,
  // Needs to be here because back button will request dismiss
  onDismissRequest: () -> Unit,
  onStart: () -> Unit,
  onCancel: () -> Unit,
) {
  val startTime = rememberSaveable(active) { System.currentTimeMillis() }
  var remainingMillis by rememberSaveable(duration) { mutableLongStateOf(duration.inWholeMilliseconds) }
  val progress = remember {
    if (duration.inWholeMilliseconds <= 0) Animatable(1f)
    else Animatable(initialValue = ((System.currentTimeMillis() - startTime).toFloat() / duration.inWholeMilliseconds.toFloat()))
  }

  LaunchedEffect(active) {
    // Animate progress indicator
    if (active) {
      val remainingMillis = duration.inWholeMilliseconds - (System.currentTimeMillis() - startTime)

      progress.animateTo(
        targetValue = 1f, animationSpec = tween(
          durationMillis = remainingMillis.toInt(), easing = LinearEasing
        )
      )
    }
  }

  DisposableEffect(active) {
    // Update clock text
    val timer = if (active) timer(period = 1.seconds.inWholeMilliseconds) {
      remainingMillis = duration.inWholeMilliseconds - (System.currentTimeMillis() - startTime)
    }
    else null

    onDispose {
      timer?.cancel()
      timer?.purge()
    }
  }

  Content(
    show = show,
    title = title,
    active = active,
    clockText = remainingMillis.milliseconds.toClockString(),
    progressPercent = progress.value,
    onDismissRequest = onDismissRequest,
    onStart = onStart,
    onCancel = onCancel,
  )
}

@Composable
private fun Content(
  show: Boolean,
  title: String,
  active: Boolean,
  clockText: String,
  progressPercent: Float,
  onDismissRequest: () -> Unit,
  onStart: () -> Unit,
  onCancel: () -> Unit,
) {
  GesturelessModalBottomSheet(show = show, onDismissRequest = onDismissRequest) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
      Box(
        contentAlignment = Alignment.BottomCenter, modifier = Modifier
          .fillMaxWidth(.7f)
          .weight(3f)
      ) {
        Box(
          contentAlignment = Alignment.Center, modifier = Modifier.aspectRatio(1f)
        ) {
          CircularProgressIndicator(
            progress = { progressPercent },
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
        IconButton(
          enabled = !active, onClick = onStart, colors = IconButtonDefaults.iconButtonColors(
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
      }
    }
  }
}

@Suppress("HardCodedStringLiteral")
@Preview(showBackground = true)
@Composable
private fun Preview(@PreviewParameter(CountdownSheetActiveProvider::class) active: Boolean) {
  CountdownSheet(
    show = true,
    title = "Title",
    active = active,
    duration = 5.seconds,
    onDismissRequest = {},
    onStart = {},
    onCancel = {})
}

class CountdownSheetActiveProvider(
  override val values: Sequence<Boolean> = sequenceOf(false, true)
) : PreviewParameterProvider<Boolean>