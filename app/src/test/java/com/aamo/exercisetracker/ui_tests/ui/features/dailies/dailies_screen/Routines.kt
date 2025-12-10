package com.aamo.exercisetracker.ui_tests.ui.features.dailies.dailies_screen

import androidx.compose.ui.test.onNodeWithText
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.test_utility.mockers.RoutineScheduleMocker
import com.aamo.exercisetracker.test_utility.ui.extensions.waitForDisplayed
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import com.aamo.exercisetracker.utility.extensions.date.Day
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Routines : PageTest() {
  @Before
  override fun setup() = runTest {
    super.setup()
    waitForLoading()
  }

  @Test
  fun `routines visible`() = runTest {
    val routine = Routine(name = "Routine 1").let {
      routineDao.upsert(it).let { id ->
        routineDao.upsert(RoutineScheduleMocker().modify { mock -> mock.copy(routineId = id) }
          .setDay(Day.today().getDayNumber()).mock())
        it.copy(id = id)
      }
    }
    Exercise(routineId = routine.id, name = "Exercise 1").also {
      routineDao.upsert(it)
    }

    rule.onNodeWithText(routine.name).waitForDisplayed().assertExists()
  }
}