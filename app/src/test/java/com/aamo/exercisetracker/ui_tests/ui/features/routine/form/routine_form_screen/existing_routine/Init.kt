package com.aamo.exercisetracker.ui_tests.ui.features.routine.form.routine_form_screen.existing_routine

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.test_utility.ui.rules.PageTest
import com.aamo.exercisetracker.utility.extensions.date.Day
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Suppress("HardCodedStringLiteral")
@Config(qualifiers = "w1000dp-h1000dp-480dpi")
@RunWith(RobolectricTestRunner::class)
class Init : PageTest() {
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
  fun `delete button visible`() {
    rule.onNodeWithContentDescription(getString(R.string.cd_delete_routine)).assertExists()
      .assertIsEnabled()
  }

  @Test
  fun `form fields`() {
    assertEquals(
      model.routine.name,
      rule.onNodeWithText(getString(R.string.label_name))
        .fetchSemanticsNode().config[SemanticsProperties.EditableText].text
    )

    val selectedDays = model.schedule!!.asListOfDays()

    Day.entries.forEach { day ->
      if (selectedDays.contains(day)) {
        rule.onNodeWithText(getString(day.nameResourceKey).take(2)).assertIsOn()
      }
      else rule.onNodeWithText(getString(day.nameResourceKey).take(2)).assertIsOff()
    }
  }
}