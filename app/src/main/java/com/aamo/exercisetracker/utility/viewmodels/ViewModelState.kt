package com.aamo.exercisetracker.utility.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ViewModelState<T>(initValue: T) {
  var value by mutableStateOf(initValue)
    private set

  private var onChange: ((T) -> Unit)? = null
  private var validationPredicate: ((T) -> T?)? = null

  fun update(value: T): T {
    val validation = validationPredicate
    val newValue = if (validation != null) validation(value) else value

    if (this.value != newValue && newValue != null) {
      this.value = newValue
      onChange?.invoke(this.value)
    }

    return this.value
  }

  /**
   * Adds change function to the state
   */
  fun onChange(function: (T) -> Unit): ViewModelState<T> {
    onChange = function
    return this
  }

  /**
   * Adds validation predicate to the state
   */
  fun validation(predicate: (T) -> T?): ViewModelState<T> {
    validationPredicate = predicate
    return this
  }
}