package com.aamo.exercisetracker.utility.viewmodels

import junit.framework.TestCase.assertEquals
import org.junit.Test

class SavingStateTests {
  @Test
  fun `getAsSaving test`() {
    val state = SavingState(canSave = { false }, unsavedChanges = true)

    state.getAsSaving().also {
      assertEquals(state.copy(state = SavingState.State.SAVING), it)
    }
  }

  @Test
  fun `getAsSaved test`() {
    val state = SavingState(canSave = { false }, unsavedChanges = true, error = Error())

    state.getAsSaved().also {
      assertEquals(
        state.copy(state = SavingState.State.SAVED, unsavedChanges = false, error = null), it
      )
    }
  }

  @Test
  fun `getAsError test`() {
    val state = SavingState(canSave = { false }, unsavedChanges = true)
    val error = Error()

    state.getAsError(error).also {
      assertEquals(state.copy(state = SavingState.State.ERROR, error = error), it)
    }
  }
}