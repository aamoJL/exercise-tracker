package com.aamo.exercisetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

/**
 * @see CircularProgressIndicator
 *
 * @param progress the progress of this progress indicator, where 0.0 represents no progress and 1.0
 *   represents full progress. Values outside of this range are coerced into the range.
 * @param segments number of segments on the indicator, minimum number of segments is 1
 * @param modifier the [Modifier] to be applied to this progress indicator
 * @param color color of this progress indicator
 * @param strokeWidth stroke width of this progress indicator
 * @param trackColor color of the track behind the indicator, visible when the progress has not
 *   reached the area of the overall indicator yet
 * @param strokeCap stroke cap to use for the ends of this progress indicator
 * @param gapSize size of the gap between the progress indicator and the track
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedCircularProgressIndicator(
  progress: () -> Float,
  segments: Int = 1,
  modifier: Modifier = Modifier,
  color: Color = ProgressIndicatorDefaults.circularColor,
  strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
  trackColor: Color = ProgressIndicatorDefaults.circularDeterminateTrackColor,
  strokeCap: StrokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
  gapSize: Dp = ProgressIndicatorDefaults.CircularIndicatorTrackGapSize,
) {
  val segments = max(segments, 1)
  // Start at 12 o'clock
  val coercedProgress = progress().coerceIn(0f, 1f)
  val stroke = with(LocalDensity.current) { Stroke(width = strokeWidth.toPx(), cap = strokeCap) }
  val circularIndicatorDiameter = 48.0.dp - 4.0.dp * 2

  Canvas(modifier
    .semantics(mergeDescendants = true) {
      progressBarRangeInfo = ProgressBarRangeInfo(coercedProgress, 0f..1f)
    }
    .size(circularIndicatorDiameter)) {
    val sweep = coercedProgress * 360f
    val adjustedGapSize = if (strokeCap == StrokeCap.Butt || size.height > size.width) {
      gapSize
    }
    else {
      gapSize + strokeWidth
    }
    val gapSizeSweep =
      if (segments == 1) 0f else (adjustedGapSize.value / (Math.PI * size.width.toDp().value).toFloat()) * 360f

    val startAngle = 270f
    val gapDeltaSweep = gapSizeSweep / 2
    val segmentAngle = 360f / segments
    val segmentSweep = segmentAngle - gapSizeSweep
    var angleCursor = startAngle

    val completeProgressSegments = (sweep / segmentAngle).toInt()
    val fractionSegment =
      floor(((sweep - segmentAngle * completeProgressSegments) / segmentAngle) * 1000) / 1000
    val incompleteSegments = (segments - completeProgressSegments - ceil(fractionSegment)).toInt()

    repeat(completeProgressSegments) { i ->
      angleCursor += gapDeltaSweep
      drawCircularIndicator(
        startAngle = angleCursor, sweep = segmentSweep, color = color, stroke = stroke
      )
      angleCursor += (segmentSweep + gapDeltaSweep)
    }

    if (fractionSegment > 0) {
      val progressSegment = segmentSweep * fractionSegment
      val incompleteSegment = segmentSweep - progressSegment

      angleCursor += gapDeltaSweep
      angleCursor += progressSegment
      drawCircularIndicator(
        startAngle = angleCursor, sweep = incompleteSegment, color = trackColor, stroke = stroke
      )
      angleCursor -= progressSegment
      drawCircularIndicator(
        startAngle = angleCursor, sweep = progressSegment, color = color, stroke = stroke
      )
      angleCursor += (progressSegment + incompleteSegment + gapDeltaSweep)
    }

    repeat(incompleteSegments) { i ->
      angleCursor += gapDeltaSweep
      drawCircularIndicator(
        startAngle = angleCursor, sweep = segmentSweep, color = trackColor, stroke = stroke
      )
      angleCursor += (segmentSweep + gapDeltaSweep)
    }
  }
}

private fun DrawScope.drawCircularIndicator(
  startAngle: Float, sweep: Float, color: Color, stroke: Stroke
) {
  // To draw this circle we need a rect with edges that line up with the midpoint of the stroke.
  // To do this we need to remove half the stroke width from the total diameter for both sides.
  val diameterOffset = stroke.width / 2
  val arcDimen = size.width - 2 * diameterOffset
  drawArc(
    color = color,
    startAngle = startAngle,
    sweepAngle = sweep,
    useCenter = false,
    topLeft = Offset(diameterOffset, diameterOffset),
    size = Size(arcDimen, arcDimen),
    style = stroke
  )
}

@Preview
@Composable
fun Preview() {
  ExerciseTrackerTheme {
    Box(
      contentAlignment = Alignment.Center, modifier = Modifier.aspectRatio(1f)
    ) {
      SegmentedCircularProgressIndicator(
        progress = { .6f },
        segments = 5,
        strokeWidth = 20.dp,
        gapSize = 10.dp,
        strokeCap = StrokeCap.Butt,
        modifier = Modifier.fillMaxSize()
      )
    }
  }
}