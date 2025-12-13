package com.aamo.exercisetracker.tests.features.routine.form.routine_form_viewmodel

import com.aamo.exercisetracker.features.routine.form.RoutineFormViewModel
import com.aamo.exercisetracker.features.routine.form.models.RoutineFormFields
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

@Suppress("HardCodedStringLiteral")
class FormState {
  @Test
  fun canSave() {
    val fields = RoutineFormFields(name = "Routine 1", days = listOf(Day.SUNDAY))

    assertTrue(RoutineFormViewModel.FormState(fields = fields).canSave())
    assertTrue(RoutineFormViewModel.FormState(fields = fields.copy(days = emptyList())).canSave())

    assertFalse(RoutineFormViewModel.FormState(fields = fields.copy(name = String.EMPTY)).canSave())
    assertFalse(
      RoutineFormViewModel.FormState(fields = fields)
        .apply { savingState = savingState.getAsSaving() }.canSave()
    )
  }

  @Test
  fun unsavedChanges() {
    val fields = RoutineFormFields(name = "Routine 1", days = listOf(Day.SUNDAY))

    assertFalse(RoutineFormViewModel.FormState(fields = fields).savingState.unsavedChanges)

    assertTrue(
      RoutineFormViewModel.FormState(fields = fields)
        .apply { routineName.update("New name") }.savingState.unsavedChanges
    )
    assertTrue(
      RoutineFormViewModel.FormState(fields = fields)
        .apply { selectedDays.add(Day.MONDAY) }.savingState.unsavedChanges
    )
  }

  @Test
  fun `selected days unique constraint`() {
    val formState = RoutineFormViewModel.FormState(
      fields = RoutineFormFields(name = "Routine 1", days = emptyList())
    )
    val day = Day.SUNDAY

    assertTrue(formState.selectedDays.values.isEmpty())

    formState.selectedDays.add(day)
    assertEquals(listOf(day), formState.selectedDays.values)

    formState.selectedDays.add(day)
    assertEquals(listOf(day), formState.selectedDays.values)
  }
}