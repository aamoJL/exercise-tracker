package com.aamo.exercisetracker.tests.utility.viewmodels.stopwatch_timer

import com.aamo.exercisetracker.services.IStopwatchTimerService
import com.aamo.exercisetracker.utility.viewmodels.IClock
import com.aamo.exercisetracker.utility.viewmodels.StopwatchTimer
import junit.framework.TestCase
import org.junit.Assert.assertNotEquals
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class Stop {
  @Test
  fun `onFinished called`() {
    var called = false

    val timer = StopwatchTimer(clock = object : IClock {
      override fun now(): Long {
        return 1L
      }
    })

    timer.start(
      onStart = { /* Nothing here */ },
      onFinished = { called = true },
      onCleanUp = { /* Nothing here */ },
      backgroundService = null,
    )

    timer.stop()
    TestCase.assertTrue(called)
  }

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
      onFinished = { /* Nothing here */ },
      onCleanUp = { called = true },
      backgroundService = null,
    )

    timer.stop()
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
      onFinished = { /* Nothing here */ },
      onCleanUp = { /* Nothing here */ },
      backgroundService = object : IStopwatchTimerService {
        override fun cancel() {
          called = true
        }
      },
    )

    timer.stop()
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
      onFinished = { /* Nothing here */ },
      onCleanUp = { /* Nothing here */ },
      backgroundService = null,
    )

    timer.stop()
    TestCase.assertFalse(timer.isRunning)
  }

  @Test
  fun `final duration is correct`() {
    var actual: Duration? = null

    val clock = object : IClock {
      var time = 1.minutes.inWholeMilliseconds

      override fun now(): Long {
        return time
      }
    }
    val timer = StopwatchTimer(clock = clock)

    val start = clock.now()
    timer.start(
      onStart = { /* Nothing here */ },
      onFinished = { actual = it },
      onCleanUp = { /* Nothing here */ },
      backgroundService = null,
    )

    clock.time += 5.minutes.inWholeMilliseconds

    val end = clock.time
    assertNotEquals(start, end)

    timer.stop()
    checkNotNull(actual)
    TestCase.assertEquals((end - start).milliseconds, actual)
  }
}