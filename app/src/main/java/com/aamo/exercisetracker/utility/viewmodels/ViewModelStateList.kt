package com.aamo.exercisetracker.utility.viewmodels

import androidx.compose.runtime.mutableStateListOf
import com.aamo.exercisetracker.utility.extensions.general.onTrue

class ViewModelStateList<T> {
  private var validationPredicate: ((T) -> T?)? = null
  private var onChange: (() -> Unit)? = null

  private val _values = mutableStateListOf<T>()
  val values: List<T> = _values

  fun add(vararg items: T) {
    items.map { item -> validationPredicate?.invoke(item) ?: item }.let {
      _values.addAll(it).onTrue {
        onChange?.invoke()
      }
    }
  }

  fun remove(vararg items: T) {
    _values.removeAll(items).onTrue {
      onChange?.invoke()
    }
  }

  fun clear() {
    _values.clear()
    onChange?.invoke()
  }

  /**
   * Adds change function to the state
   */
  fun onChange(function: () -> Unit): ViewModelStateList<T> {
    onChange = function
    return this
  }

  /**
   * Adds validation predicate to the state
   */
  fun validation(predicate: (T) -> T?): ViewModelStateList<T> {
    validationPredicate = predicate
    return this
  }
}