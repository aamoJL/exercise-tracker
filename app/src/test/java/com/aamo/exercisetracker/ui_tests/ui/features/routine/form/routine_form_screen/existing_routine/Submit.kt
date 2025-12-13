package com.aamo.exercisetracker.ui_tests.ui.features.routine.form.routine_form_screen.existing_routine

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import com.aamo.exercisetracker.utility.extensions.date.Day
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class Submit : PageTest() {
  lateinit var model: RoutineWithSchedule

  @Before
  override fun setup() = runTest {
    super.setup()
    model = toRoutineFormScreen(
      model = RoutineWithSchedule(
        routine = Routine(name = "Routine 1"),
        schedule = RoutineSchedule(routineId = 0L, sunday = true)
      )
    ) ?: throw Error()
    waitForLoading()
  }

  @Test
  fun submit() = runTest {
    val newName = "Routine 1"
    val newDays = listOf(Day.MONDAY, Day.WEDNESDAY)

    rule.onNodeWithContentDescription(getString(R.string.cd_save_routine)).assertIsEnabled()

    rule.onNodeWithText(getString(R.string.label_name)).performTextReplacement(newName)
    newDays.forEach {
      rule.onNodeWithText(getString(it.nameResourceKey).take(2)).performClick()
    }

    rule.onNodeWithContentDescription(getString(R.string.cd_save_routine)).assertIsEnabled()
      .performClick()
    waitForLoading()

    rule.onNodeWithContentDescription(getString(R.string.cd_edit_routine)).assertExists()
  }
}