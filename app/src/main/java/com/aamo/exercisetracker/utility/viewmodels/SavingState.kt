package com.aamo.exercisetracker.utility.viewmodels

data class SavingState(
  val state: State = State.NONE,
  val canSave: () -> Boolean = { true },
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
    return copy(state = State.SAVING, error = null)
  }

  fun getAsSaved(): SavingState {
    return copy(state = State.SAVED, unsavedChanges = false, error = null)
  }

  fun getAsError(error: Error): SavingState {
    return copy(state = State.ERROR, error = error)
  }
}