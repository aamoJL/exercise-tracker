package com.aamo.exercisetracker.utility.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ViewModelState<T>(initValue: T) {
  var value by mutableStateOf(initValue)
    private set

  private var onChange: ((T) -> Unit)? = null
  private var validationPredicate: ((T) -> T?)? = null

  fun update(value: T) {
    val value = validationPredicate?.invoke(value) ?: value

    if (this.value != value && value != null) {
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
  fun validation(predicate: (T) -> T?): ViewModelState<T> {
    validationPredicate = predicate
    return this
  }
}