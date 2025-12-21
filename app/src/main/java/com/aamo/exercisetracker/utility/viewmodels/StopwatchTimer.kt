package com.aamo.exercisetracker.utility.viewmodels

import com.aamo.exercisetracker.services.IStopwatchTimerService
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

interface IClock {
  fun now(): Long
}

class BasicClock : IClock {
  override fun now(): Long {
    return System.currentTimeMillis()
  }
}

class StopwatchTimer(val clock: IClock = BasicClock()) {
  private data class Properties(
    val startTime: Long,
    val onFinished: (Duration) -> Unit,
    val onCleanUp: () -> Unit,
    val backgroundService: IStopwatchTimerService?
  )

  private var properties: Properties? = null

  val isRunning get() = properties != null

  fun start(
    onStart: () -> Unit,
    onFinished: (Duration) -> Unit,
    onCleanUp: () -> Unit,
    backgroundService: IStopwatchTimerService? = null
  ) {
    if (isRunning) cancel()

    val startTime = clock.now()

    properties = Properties(
      startTime = startTime,
      onFinished = onFinished,
      onCleanUp = onCleanUp,
      backgroundService = backgroundService
    )
    onStart()
    backgroundService?.start(startTime)
  }

  fun stop() {
    val endTime = clock.now()

    properties?.apply {
      onFinished.also { cancel() }.invoke((endTime - startTime).milliseconds)
    }
  }

  fun cancel() {
    properties?.apply {
      onCleanUp()
      backgroundService?.cancel()
    }
    properties = null
  }
}