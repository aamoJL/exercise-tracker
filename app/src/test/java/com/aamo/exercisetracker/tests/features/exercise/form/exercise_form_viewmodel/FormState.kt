package com.aamo.exercisetracker.tests.features.exercise.form.exercise_form_viewmodel

import com.aamo.exercisetracker.features.exercise.form.ExerciseFormViewModel
import com.aamo.exercisetracker.features.exercise.form.models.ExerciseFormFields
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

@Suppress("HardCodedStringLiteral")
class FormState {
  @Test
  fun canSave() {
    val fields = ExerciseFormFields(
      name = "Exercise 1",
      restDuration = 3.minutes,
      unit = "Unit",
      setValues = listOf(1, 2, 3),
      hasTimer = false
    )

    listOf(
      ExerciseFormViewModel.FormState(fields = fields),
      ExerciseFormViewModel.FormState(fields = fields.copy(restDuration = 0.minutes)),
      ExerciseFormViewModel.FormState(fields = fields.copy(hasTimer = true, unit = String.EMPTY)),
      ExerciseFormViewModel.FormState(fields = fields.copy(hasTimer = true)),
    ).forEach {
      assertTrue(it.canSave())
    }

    listOf(
      ExerciseFormViewModel.FormState(fields = fields.copy(name = String.EMPTY)),
      ExerciseFormViewModel.FormState(fields = fields.copy(hasTimer = false, unit = String.EMPTY)),
      ExerciseFormViewModel.FormState(fields = fields.copy(setValues = emptyList())),
      ExerciseFormViewModel.FormState(fields = fields.copy(restDuration = 0.minutes))
        .apply { hasRest.update(true) },
      ExerciseFormViewModel.FormState(fields = fields)
        .apply { savingState = savingState.getAsSaving() },
    ).forEachIndexed { i, fields ->
      assertFalse(i.toString(), fields.canSave())
    }
  }

  @Test
  fun unsavedChanges() {
    val fields = ExerciseFormFields(
      name = "Exercise 1",
      restDuration = 3.minutes,
      unit = "Unit",
      setValues = listOf(1, 2, 3),
      hasTimer = false
    )

    assertFalse(ExerciseFormViewModel.FormState(fields = fields).savingState.unsavedChanges)

    listOf(
      ExerciseFormViewModel.FormState(fields = fields).apply { exerciseName.update("Update") },
      ExerciseFormViewModel.FormState(fields = fields).apply { restDuration.update(0.minutes) },
      ExerciseFormViewModel.FormState(fields = fields).apply { setUnit.update("Update") },
      ExerciseFormViewModel.FormState(fields = fields).apply { addSetValue() },
      ExerciseFormViewModel.FormState(fields = fields)
        .apply { setValues.values.first().value.update(10) },
      ExerciseFormViewModel.FormState(fields = fields).apply { hasTimer.update(true) },
      ExerciseFormViewModel.FormState(fields = fields).apply { hasRest.update(false) },
    ).forEach {
      assertTrue(it.savingState.unsavedChanges)
    }
  }
}