package com.aamo.exercisetracker.ui_tests.ui.features.dailies.dailies_screen

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.test_utility.mockers.RoutineScheduleMocker
import com.aamo.exercisetracker.test_utility.ui.extensions.waitForDisplayed
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOrder
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Calendar

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Navigation : PageTest() {
  @Before
  override fun setup() = runTest {
    super.setup()
    waitForLoading()
  }

  @Test
  fun `on dailiesScreen`() = runTest {
    rule.onNodeWithText(getString(Day.today().nameResourceKey)).assertExists()
  }

  @Test
  fun `to routinePage`() = runTest {
    val routine = Routine(name = "Routine 1").let {
      routineDao.upsert(it).let { id ->
        routineDao.upsert(RoutineScheduleMocker().modify { mock -> mock.copy(routineId = id) }
          .setDay(Day.today().getDayNumber()).mock())
        it.copy(id = id)
      }
    }
    val exercise = Exercise(routineId = routine.id, name = "Exercise 1").also {
      routineDao.upsert(it)
    }

    rule.onNodeWithText(routine.name).waitForDisplayed().performClick()
    waitForLoading()
    rule.onNodeWithText(exercise.name).waitForDisplayed().assertExists()
  }

  @Test
  fun `to progressTrackingPage`() = runTest {
    val progress = TrackedProgress(name = "Tracked Progress 1", intervalWeeks = 1).also {
      trackedProgressDao.upsert(it)
    }

    rule.onNodeWithContentDescription(getString(R.string.cd_show_unfinished_tracked_progresses))
      .waitForDisplayed().performClick()
    rule.onNodeWithText(progress.name).waitForDisplayed().performClick()
    waitForLoading()
    rule.onNodeWithContentDescription(getString(R.string.cd_add)).waitForDisplayed().assertExists()
  }

  @Test
  fun `switch day`() {
    val days = Calendar.getInstance().getLocalDayOrder()
    val initDayIndex = days.indexOf(Day.today())

    // this is like this because I could not find a way to change Robolectric date
    if (initDayIndex == 0) {
      rule.onNodeWithText(getString(days[0].nameResourceKey))
      rule.onRoot().performTouchInput { swipeLeft() }
      rule.onNodeWithText(getString(days[1].nameResourceKey))
      rule.onRoot().performTouchInput { swipeRight() }
      rule.onNodeWithText(getString(days[0].nameResourceKey))
    }
    else {
      rule.onNodeWithText(getString(days[initDayIndex].nameResourceKey))
      rule.onRoot().performTouchInput { swipeRight() }
      rule.onNodeWithText(getString(days[initDayIndex + 1].nameResourceKey))
      rule.onRoot().performTouchInput { swipeLeft() }
      rule.onNodeWithText(getString(days[initDayIndex].nameResourceKey))
    }
  }
}