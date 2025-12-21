package com.aamo.exercisetracker.tests.utility.viewmodels.countdown_timer

import com.aamo.exercisetracker.services.ICountdownTimerService
import com.aamo.exercisetracker.utility.viewmodels.CountdownTimer
import com.aamo.exercisetracker.utility.viewmodels.ITimer
import junit.framework.TestCase
import junit.framework.TestCase.fail
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class Start {
  @Test
  fun `timer started`() {
    var called = false

    val timer = CountdownTimer(timer = object : ITimer {
      override fun start(duration: Duration, onFinished: () -> Unit) {
        called = true
      }
    })

    TestCase.assertFalse(called)

    timer.start(
      duration = 1.minutes,
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { fail() },
      backgroundService = null
    )

    TestCase.assertTrue(called)
  }

  @Test
  fun `onStart called`() {
    var called = false

    val timer = CountdownTimer(timer = object : ITimer {
      override fun start(duration: Duration, onFinished: () -> Unit) {}
    })

    TestCase.assertFalse(called)

    timer.start(
      duration = 1.minutes,
      onStart = { called = true },
      onFinished = { fail() },
      onCleanUp = { fail() },
      backgroundService = null
    )

    TestCase.assertTrue(called)
  }

  @Test
  fun `background service started`() {
    var called = false

    val timer = CountdownTimer(timer = object : ITimer {
      override fun start(duration: Duration, onFinished: () -> Unit) {}
    })

    TestCase.assertFalse(called)

    timer.start(
      duration = 1.minutes,
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { fail() },
      backgroundService = object : ICountdownTimerService {
        override fun start(duration: Duration) {
          called = true
        }
      })

    TestCase.assertTrue(called)
  }

  @Test
  fun `isRunning is true`() {
    val timer = CountdownTimer(timer = object : ITimer {})

    TestCase.assertFalse(timer.isRunning)

    timer.start(
      duration = 1.minutes,
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { fail() },
      backgroundService = null
    )

    TestCase.assertTrue(timer.isRunning)
  }

  @Test
  fun `previous timer cancelled`() {
    var cancelled = false

    val timer = CountdownTimer(timer = object : ITimer {})

    TestCase.assertFalse(cancelled)

    timer.start(
      duration = 1.minutes,
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { cancelled = true },
      backgroundService = null
    )
    TestCase.assertFalse(cancelled)

    timer.start(
      duration = 1.minutes,
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { fail() },
      backgroundService = null
    )
    TestCase.assertTrue(cancelled)
  }

  @Test
  fun `start duration is correct`() {
    var actual: Duration? = null

    val timer = CountdownTimer(timer = object : ITimer {
      override fun start(duration: Duration, onFinished: () -> Unit) {
        actual = duration
      }
    })

    val duration = 1.minutes
    timer.start(
      duration = duration,
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { fail() },
      backgroundService = null
    )

    checkNotNull(actual)
    TestCase.assertEquals(duration, actual)
  }

  @Test
  fun `background service duration is correct`() {
    var actual: Duration? = null

    val timer = CountdownTimer(timer = object : ITimer {})

    val duration = 1.minutes
    timer.start(
      duration = duration,
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { fail() },
      backgroundService = object : ICountdownTimerService {
        override fun start(duration: Duration) {
          actual = duration
        }
      })

    checkNotNull(actual)
    TestCase.assertEquals(duration, actual)
  }
}