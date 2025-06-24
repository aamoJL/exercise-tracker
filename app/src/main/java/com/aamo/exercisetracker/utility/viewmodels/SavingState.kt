package com.aamo.exercisetracker.utility.viewmodels

data class SavingState(
  val state: State = State.NONE,
  val canSave: Boolean = false,
  val unsavedChanges: Boolean = false,
  val error: Error? = null
) {
  enum class State {
    NONE,
    SAVING,
    SAVED,
    ERROR,
  }

  fun getAsSaving(): SavingState {
    return copy(state = State.SAVING, canSave = false, error = null)
  }

  fun getAsSaved(canSave: Boolean): SavingState {
    return copy(state = State.SAVED, canSave = canSave, unsavedChanges = false, error = null)
  }

  fun getAsError(error: Error): SavingState {
    return copy(state = State.ERROR, error = error)
  }
}