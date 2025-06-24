package com.aamo.exercisetracker.utility.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ViewModelState<T>(initValue: T) {
  var value by mutableStateOf(initValue)
    private set

  private var onChange: ((T) -> Unit)? = null
  private var validationPredicate: ((T) -> Boolean)? = null

  fun update(value: T) {
    if (this.value != value && validationPredicate?.invoke(value) != false) {
      this.value = value
      onChange?.invoke(value)
    }
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
  fun validation(predicate: (T) -> Boolean): ViewModelState<T> {
    validationPredicate = predicate
    return this
  }
}