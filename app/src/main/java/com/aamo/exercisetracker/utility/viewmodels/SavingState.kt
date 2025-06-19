package com.aamo.exercisetracker.utility.viewmodels

import com.aamo.exercisetracker.utility.extensions.string.EMPTY

data class SavingState(
  val state: State,
  val msg: String = String.EMPTY,
  val canSave: Boolean = false,
  val unsavedChanges: Boolean = false
) {
  enum class State {
    NONE,
    SAVING,
    SAVED,
    ERROR,
  }
}