package com.aamo.exercisetracker.features.exercise.view.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.ui.components.SegmentedCircularProgressIndicator
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import kotlin.math.min

@Composable
fun SetProgressIndicator(currentSetNumber: Int, totalSets: Int) {
  val progress by remember(currentSetNumber) {
    mutableFloatStateOf(
      if (totalSets == 0) 0f else (currentSetNumber.toFloat() - 1) / totalSets.toFloat()
    )
  }
  val animatedProgress by animateFloatAsState(
    targetValue = progress, animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
  )

  Box(
    contentAlignment = Alignment.Center, modifier = Modifier
      .fillMaxWidth(.7f)
      .aspectRatio(1f)
  ) {
    SegmentedCircularProgressIndicator(
      progress = { animatedProgress },
      segments = totalSets,
      strokeWidth = 20.dp,
      gapSize = 10.dp,
      strokeCap = StrokeCap.Round,
      modifier = Modifier.fillMaxSize()
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = ifElse(
          condition = currentSetNumber > totalSets,
          ifTrue = { stringResource(R.string.title_completed) },
          ifFalse = { stringResource(R.string.title_set) }),
        style = MaterialTheme.typography.titleMedium
      )
      Text(
        text = "${min(currentSetNumber, totalSets)}/$totalSets",
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