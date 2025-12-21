package com.aamo.exercisetracker.tests.features.progress_tracking.form.tracked_progress_form_viewmodel

import com.aamo.exercisetracker.features.progress_tracking.form.TrackedProgressFormViewModel
import com.aamo.exercisetracker.features.progress_tracking.form.models.TrackedProgressFormFields
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertNotEquals
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Suppress("HardCodedStringLiteral")
class FormState {
  @Test
  fun canSave() {
    val fields = TrackedProgressFormFields(
      name = "Progress 1",
      weeklyInterval = 1,
      type = TrackedProgressFormFields.ProgressType.REPETITION,
      progressValueUnit = "Unit",
      timerDuration = 4.minutes
    )

    assertTrue(TrackedProgressFormViewModel.FormState(fields = fields).canSave())
    assertTrue(
      TrackedProgressFormViewModel.FormState(fields = fields.copy(weeklyInterval = 0)).canSave()
    )
    assertTrue(
      TrackedProgressFormViewModel.FormState(fields = fields.copy(progressValueUnit = String.EMPTY))
        .canSave()
    )
    assertTrue(
      TrackedProgressFormViewModel.FormState(
        fields = fields.copy(
          type = TrackedProgressFormFields.ProgressType.REPETITION, timerDuration = null
        )
      ).canSave()
    )
    assertTrue(
      TrackedProgressFormViewModel.FormState(
        fields = fields.copy(
          type = TrackedProgressFormFields.ProgressType.STOPWATCH, timerDuration = null
        )
      ).canSave()
    )
    assertTrue(
      TrackedProgressFormViewModel.FormState(
        fields = fields.copy(
          type = TrackedProgressFormFields.ProgressType.TIMER, timerDuration = 1.minutes
        )
      ).canSave()
    )

    assertFalse(
      TrackedProgressFormViewModel.FormState(fields = fields.copy(name = String.EMPTY)).canSave()
    )
    assertFalse(
      TrackedProgressFormViewModel.FormState(
        fields = fields.copy(
          type = TrackedProgressFormFields.ProgressType.TIMER, timerDuration = 0.seconds
        )
      ).canSave()
    )
    assertFalse(
      TrackedProgressFormViewModel.FormState(fields = fields).apply {
        savingState = savingState.getAsSaving()
      }.canSave()
    )
  }

  @Test
  fun unsavedChanges() {
    val fields = TrackedProgressFormFields(
      name = "Progress 1",
      weeklyInterval = 1,
      type = TrackedProgressFormFields.ProgressType.REPETITION,
      progressValueUnit = "Unit",
      timerDuration = 4.minutes
    )

    assertFalse(TrackedProgressFormViewModel.FormState(fields = fields).savingState.unsavedChanges)

    assertTrue(
      TrackedProgressFormViewModel.FormState(fields = fields).apply {
        progressName.update("New name")
      }.savingState.unsavedChanges
    )
    assertTrue(
      TrackedProgressFormViewModel.FormState(fields = fields).apply {
        weeklyInterval.update(2)
      }.savingState.unsavedChanges
    )
    assertTrue(
      TrackedProgressFormViewModel.FormState(fields = fields).apply {
        progressType.update(TrackedProgressFormFields.ProgressType.STOPWATCH)
      }.savingState.unsavedChanges
    )
    assertTrue(
      TrackedProgressFormViewModel.FormState(fields = fields).apply {
        progressValueUnit.update("New unit")
      }.savingState.unsavedChanges
    )
    assertTrue(
      TrackedProgressFormViewModel.FormState(fields = fields).apply {
        timerDuration.update(2.minutes)
      }.savingState.unsavedChanges
    )
  }

  @Test
  fun `progress type change changes timer duration if type is not timer`() {
    val formState = TrackedProgressFormViewModel.FormState(
      fields = TrackedProgressFormFields(
        name = "Progress 1",
        weeklyInterval = 1,
        type = TrackedProgressFormFields.ProgressType.TIMER,
        progressValueUnit = "Unit",
        timerDuration = 4.minutes
      )
    )

    assertTrue(formState.timerDuration.value > 0.seconds)
    assertEquals(TrackedProgressFormFields.ProgressType.TIMER, formState.progressType.value)

    formState.progressType.update(TrackedProgressFormFields.ProgressType.REPETITION)
    assertEquals(0.seconds, formState.timerDuration.value)
    assertNotEquals(TrackedProgressFormFields.ProgressType.TIMER, formState.progressType.value)
  }
}