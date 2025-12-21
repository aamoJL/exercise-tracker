package com.aamo.exercisetracker.tests.utility.viewmodels.stopwatch_timer

import com.aamo.exercisetracker.services.IStopwatchTimerService
import com.aamo.exercisetracker.utility.viewmodels.IClock
import com.aamo.exercisetracker.utility.viewmodels.StopwatchTimer
import junit.framework.TestCase
import junit.framework.TestCase.fail
import org.junit.Test

class Cancel {
  @Test
  fun `onCleanUp called`() {
    var called = false

    val timer = StopwatchTimer(clock = object : IClock {
      override fun now(): Long {
        return 1L
      }
    })

    timer.start(
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { called = true },
      backgroundService = null,
    )

    timer.cancel()
    TestCase.assertTrue(called)
  }

  @Test
  fun `background service cancelled`() {
    var called = false

    val timer = StopwatchTimer(clock = object : IClock {
      override fun now(): Long {
        return 1L
      }
    })

    timer.start(
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { /* Nothing here */ },
      backgroundService = object : IStopwatchTimerService {
        override fun cancel() {
          called = true
        }
      },
    )

    timer.cancel()
    TestCase.assertTrue(called)
  }

  @Test
  fun `isRunning is false`() {
    val timer = StopwatchTimer(clock = object : IClock {
      override fun now(): Long {
        return 1L
      }
    })

    timer.start(
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { /* Nothing here */ },
      backgroundService = null,
    )

    timer.cancel()
    TestCase.assertFalse(timer.isRunning)
  }
}