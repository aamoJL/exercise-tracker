package com.aamo.exercisetracker.tests.utility.viewmodels.countdown_timer

import com.aamo.exercisetracker.services.ICountdownTimerService
import com.aamo.exercisetracker.utility.viewmodels.CountdownTimer
import com.aamo.exercisetracker.utility.viewmodels.ITimer
import junit.framework.TestCase
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class TimerFinished {
  @Test
  fun `timer stopped`() {
    var called = false
    var finish: (() -> Unit)? = null

    val timer = CountdownTimer(timer = object : ITimer {
      override fun start(duration: Duration, onFinished: () -> Unit) {
        finish = onFinished
      }

      override fun cleanup() {
        called = true
      }
    })

    timer.start(
      duration = 1.minutes,
      onStart = { /* Nothing here */ },
      onFinished = { /* Nothing here */ },
      onCleanUp = { /* Nothing here */ },
      backgroundService = null
    )
    TestCase.assertFalse(called)

    checkNotNull(finish).invoke()
    TestCase.assertTrue(called)
  }

  @Test
  fun `onFinished called`() {
    var called = false
    var finish: (() -> Unit)? = null

    val timer = CountdownTimer(timer = object : ITimer {
      override fun start(duration: Duration, onFinished: () -> Unit) {
        finish = onFinished
      }
    })

    timer.start(
      duration = 1.minutes,
      onStart = { /* Nothing here */ },
      onFinished = { called = true },
      onCleanUp = { /* Nothing here */ },
      backgroundService = null
    )
    TestCase.assertFalse(called)

    checkNotNull(finish).invoke()
    TestCase.assertTrue(called)
  }

  @Test
  fun `onCleanUp called`() {
    var called = false
    var finish: (() -> Unit)? = null

    val timer = CountdownTimer(timer = object : ITimer {
      override fun start(duration: Duration, onFinished: () -> Unit) {
        finish = onFinished
      }
    })

    timer.start(
      duration = 1.minutes,
      onStart = { /* Nothing here */ },
      onFinished = { /* Nothing here */ },
      onCleanUp = { called = true },
      backgroundService = null
    )
    TestCase.assertFalse(called)

    checkNotNull(finish).invoke()
    TestCase.assertTrue(called)
  }

  @Test
  fun `background service stopped`() {
    var called = false
    var finish: (() -> Unit)? = null

    val timer = CountdownTimer(timer = object : ITimer {
      override fun start(duration: Duration, onFinished: () -> Unit) {
        finish = onFinished
      }
    })

    timer.start(
      duration = 1.minutes,
      onStart = { /* Nothing here */ },
      onFinished = { /* Nothing here */ },
      onCleanUp = { /* Nothing here */ },
      backgroundService = object : ICountdownTimerService {
        override fun cancel() {
          called = true
        }
      })
    TestCase.assertFalse(called)

    checkNotNull(finish).invoke()
    TestCase.assertTrue(called)

  }

  @Test
  fun `background service finished`() {
    var called = false
    var finish: (() -> Unit)? = null

    val timer = CountdownTimer(timer = object : ITimer {
      override fun start(duration: Duration, onFinished: () -> Unit) {
        finish = onFinished
      }
    })

    timer.start(
      duration = 1.minutes,
      onStart = { /* Nothing here */ },
      onFinished = { /* Nothing here */ },
      onCleanUp = { /* Nothing here */ },
      backgroundService = object : ICountdownTimerService {
        override fun finish() {
          called = true
        }
      })
    TestCase.assertFalse(called)

    checkNotNull(finish).invoke()
    TestCase.assertTrue(called)
  }

  @Test
  fun `isRunning is false`() {
    var finish: (() -> Unit)? = null

    val timer = CountdownTimer(timer = object : ITimer {
      override fun start(duration: Duration, onFinished: () -> Unit) {
        finish = onFinished
      }
    })

    TestCase.assertFalse(timer.isRunning)

    timer.start(
      duration = 1.minutes,
      onStart = { /* Nothing here */ },
      onFinished = { /* Nothing here */ },
      onCleanUp = { /* Nothing here */ },
      backgroundService = null
    )

    checkNotNull(finish).invoke()
    TestCase.assertFalse(timer.isRunning)
  }
}