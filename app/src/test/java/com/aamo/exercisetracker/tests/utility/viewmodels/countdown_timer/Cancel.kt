package com.aamo.exercisetracker.tests.utility.viewmodels.countdown_timer

import com.aamo.exercisetracker.services.ICountdownTimerService
import com.aamo.exercisetracker.utility.viewmodels.CountdownTimer
import com.aamo.exercisetracker.utility.viewmodels.ITimer
import junit.framework.TestCase
import junit.framework.TestCase.fail
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class Cancel {
  @Test
  fun `timer cancelled`() {
    var called = false

    val timer = CountdownTimer(timer = object : ITimer {
      override fun cleanup() {
        called = true
      }
    })

    timer.start(
      duration = 1.minutes,
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { /* Nothing here */ },
      backgroundService = null
    )
    TestCase.assertFalse(called)

    timer.cancel()
    TestCase.assertTrue(called)
  }

  @Test
  fun `onCleanUp called`() {
    var called = false

    val timer = CountdownTimer(timer = object : ITimer {})

    timer.start(
      duration = 1.minutes,
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { called = true },
      backgroundService = null
    )
    TestCase.assertFalse(called)

    timer.cancel()
    TestCase.assertTrue(called)
  }

  @Test
  fun `background service cancelled`() {
    var called = false

    val timer = CountdownTimer(timer = object : ITimer {})

    timer.start(
      duration = 1.minutes,
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { /* Nothing here */ },
      backgroundService = object : ICountdownTimerService {
        override fun cancel() {
          called = true
        }

        override fun finish() {
          fail()
        }
      })
    TestCase.assertFalse(called)

    timer.cancel()
    TestCase.assertTrue(called)
  }

  @Test
  fun `isRunning is false`() {
    val timer = CountdownTimer(timer = object : ITimer {})

    TestCase.assertFalse(timer.isRunning)

    timer.start(
      duration = 1.minutes,
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { /* Nothing here */ },
      backgroundService = null
    )

    timer.cancel()
    TestCase.assertFalse(timer.isRunning)
  }
}