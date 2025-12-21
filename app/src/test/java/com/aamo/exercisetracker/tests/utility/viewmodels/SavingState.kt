package com.aamo.exercisetracker.tests.utility.viewmodels

import com.aamo.exercisetracker.utility.viewmodels.SavingState
import junit.framework.TestCase.assertEquals
import org.junit.Test

class SavingState {
  @Test
  fun getAsSaving() {
    val state = SavingState(unsavedChanges = true)

    assertEquals(state.copy(state = SavingState.State.SAVING), state.getAsSaving())
  }

  @Test
  fun getAsSaved() {
    val state = SavingState(unsavedChanges = true, error = Error())

    assertEquals(
      state.copy(state = SavingState.State.SAVED, unsavedChanges = false, error = null),
      state.getAsSaved()
    )
  }

  @Test
  fun getAsError() {
    val state = SavingState(unsavedChanges = true)
    val error = Error()

    assertEquals(
      state.copy(state = SavingState.State.ERROR, error = error), state.getAsError(error)
    )
  }
}