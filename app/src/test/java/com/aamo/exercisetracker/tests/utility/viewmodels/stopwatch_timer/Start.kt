package com.aamo.exercisetracker.tests.utility.viewmodels.stopwatch_timer

import com.aamo.exercisetracker.services.IStopwatchTimerService
import com.aamo.exercisetracker.utility.viewmodels.IClock
import com.aamo.exercisetracker.utility.viewmodels.StopwatchTimer
import junit.framework.TestCase
import junit.framework.TestCase.fail
import org.junit.Test

class Start {
  @Test
  fun `onStart called`() {
    var called = false

    val timer = StopwatchTimer(clock = object : IClock {
      override fun now(): Long {
        return 1L
      }
    })

    timer.start(
      onStart = { called = true },
      onFinished = { fail() },
      onCleanUp = { fail() },
      backgroundService = null,
    )
    TestCase.assertTrue(called)
  }

  @Test
  fun `background service started`() {
    var called = false

    val timer = StopwatchTimer(clock = object : IClock {
      override fun now(): Long {
        return 1L
      }
    })

    timer.start(
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { fail() },
      backgroundService = object : IStopwatchTimerService {
        override fun start(startTime: Long) {
          called = true
        }
      },
    )
    TestCase.assertTrue(called)
  }

  @Test
  fun `background service start time correct`() {
    var actual: Long? = null
    val time = 15L

    val timer = StopwatchTimer(clock = object : IClock {
      override fun now(): Long {
        return time
      }
    })

    timer.start(
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { fail() },
      backgroundService = object : IStopwatchTimerService {
        override fun start(startTime: Long) {
          actual = startTime
        }
      },
    )
    TestCase.assertEquals(time, actual)
  }

  @Test
  fun `previous timer is cancelled`() {
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
    TestCase.assertFalse(called)

    timer.start(
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { fail() },
      backgroundService = null,
    )

    TestCase.assertTrue(called)
  }

  @Test
  fun `isRunning is true`() {
    val timer = StopwatchTimer(clock = object : IClock {
      override fun now(): Long {
        return 1L
      }
    })

    TestCase.assertFalse(timer.isRunning)

    timer.start(
      onStart = { /* Nothing here */ },
      onFinished = { fail() },
      onCleanUp = { fail() },
      backgroundService = null,
    )
    TestCase.assertTrue(timer.isRunning)
  }
}