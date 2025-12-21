package com.aamo.exercisetracker.utility.viewmodels

import com.aamo.exercisetracker.services.ICountdownTimerService
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.time.Duration

interface ITimer {
  fun start(duration: Duration, onFinished: () -> Unit) {}
  fun cleanup() {}
}

class BasicTimer() : ITimer {
  private var timer: Timer? = null

  override fun start(duration: Duration, onFinished: () -> Unit) {
    timer = Timer(true).apply {
      schedule(timerTask {
        onFinished()
      }, duration.inWholeMilliseconds)
    }
  }

  override fun cleanup() {
    timer?.cancel()
    timer = null
  }
}

class CountdownTimer(val timer: ITimer = BasicTimer()) {
  private data class Properties(
    val onFinished: () -> Unit,
    val onCleanUp: () -> Unit,
    val backgroundService: ICountdownTimerService?
  )

  private var properties: Properties? = null

  val isRunning get() = properties != null

  fun start(
    duration: Duration,
    onStart: () -> Unit,
    onFinished: () -> Unit,
    onCleanUp: () -> Unit,
    backgroundService: ICountdownTimerService? = null,
  ) {
    if (isRunning) cancel()

    properties = Properties(
      onFinished = onFinished, onCleanUp = onCleanUp, backgroundService = backgroundService
    )
    timer.start(duration = duration, onFinished = { finish() })
    onStart()
    backgroundService?.start(duration)
  }

  private fun finish() {
    properties?.backgroundService?.finish()
    stop()
  }

  fun stop() {
    properties?.apply {
      onFinished.also { cancel() }.invoke()
    }
  }

  fun cancel() {
    timer.cleanup()
    properties?.apply {
      onCleanUp()
      backgroundService?.cancel()
    }
    properties = null
  }
}