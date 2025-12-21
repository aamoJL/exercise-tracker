package com.aamo.exercisetracker.features.progress_tracking.view.components

import android.icu.text.DecimalFormat
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.features.progress_tracking.view.models.ProgressTrackingTrackedProgressModel
import com.aamo.exercisetracker.utility.extensions.date.toClockString
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.extensions.general.trimFirst
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.extensions.format
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties
import kotlin.math.ceil
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun RecordChart(model: ProgressTrackingTrackedProgressModel, modifier: Modifier = Modifier) {
  val values = model.values.map { it.toDouble() }
  val gridProperties =
    GridProperties(xAxisProperties = GridProperties.AxisProperties(lineCount = 5))
  val maxValue = (values.maxOrNull())?.let { max ->
    val segmentCount = gridProperties.yAxisProperties.lineCount - 1

    if (model.progressType == ProgressTrackingTrackedProgressModel.ProgressType.STOPWATCH) {
      val minutes = (segmentCount / 2).minutes.inWholeMilliseconds.toDouble()
      val multiplier = ceil(max / minutes).toInt()

      multiplier * minutes
    }
    else {
      val segment = max / segmentCount.toDouble()
      val delta = segment % 10 // Get delta to next round number

      max + segmentCount * (10 - delta)
    }
  } ?: 0.toDouble()
  val label = when (model.progressType) {
    ProgressTrackingTrackedProgressModel.ProgressType.STOPWATCH -> stringResource(R.string.label_time)
    else -> model.recordUnit.replaceFirstChar { char -> char.uppercase() }
  }
  val lineColor = MaterialTheme.colorScheme.primary
  val lines = remember(values) {
    listOf(
      Line(
        label = label,
        values = ifElse(
          condition = values.isNotEmpty(),
          ifTrue = { values },
          ifFalse = { listOf(0.toDouble()) }), // LineChart will crash if the values list is empty
        color = SolidColor(lineColor),
        firstGradientFillColor = lineColor.copy(alpha = .5f),
        secondGradientFillColor = Color.Transparent,
        strokeAnimationSpec = tween(1000, easing = EaseInOutCubic),
        gradientAnimationDelay = 500,
        drawStyle = DrawStyle.Stroke(width = 2.dp),
        curvedEdges = true
      )
    )
  }
  val indicatorProperties = HorizontalIndicatorProperties(
    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface), contentBuilder = { value ->
      when (model.progressType) {
        ProgressTrackingTrackedProgressModel.ProgressType.STOPWATCH -> value.toDuration(DurationUnit.MILLISECONDS)
          .toClockString(hasHours = value >= 1.hours.inWholeMilliseconds).trimFirst('0')

        else -> value.format(0)
      }
    })
  val labelHelperProperties = LabelHelperProperties(
    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
  )
  val popupProperties = PopupProperties(
    mode = PopupProperties.Mode.PointMode(threshold = 30.dp),
    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
    contentBuilder = { popup ->
      when (model.progressType) {
        ProgressTrackingTrackedProgressModel.ProgressType.STOPWATCH -> popup.value.toDuration(
          DurationUnit.MILLISECONDS
        ).toClockString(hasHours = popup.value >= 1.hours.inWholeMilliseconds).trimFirst('0')

        else -> DecimalFormat.getInstance().apply { maximumFractionDigits = 0 }.format(popup.value)
      }
    })

  LineChart(
    modifier = modifier,
    data = lines,
    indicatorProperties = indicatorProperties,
    labelHelperProperties = labelHelperProperties,
    popupProperties = popupProperties,
    maxValue = maxValue,
    gridProperties = gridProperties
  )
}

@Suppress("HardCodedStringLiteral")
@Preview(showBackground = true)
@Composable
private fun Preview() {
  RecordChart(
    model = ProgressTrackingTrackedProgressModel(
      id = 1L,
      name = "Progress 1",
      progressType = ProgressTrackingTrackedProgressModel.ProgressType.REPETITION,
      values = listOf(1, 2, 3),
      recordUnit = "Reps",
      countdownTime = null
    )
  )
}