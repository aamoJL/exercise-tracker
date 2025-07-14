package com.aamo.exercisetracker.utility.viewmodels

import androidx.compose.runtime.mutableStateListOf
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.extensions.general.onTrue

class ViewModelStateList<T> {
  private var validationPredicate: ((T) -> T?)? = null
  private var onChange: (() -> Unit)? = null

  private val _values = mutableStateListOf<T>()
  val values: List<T> = _values

  fun add(vararg items: T) {
    items.mapNotNull { item ->
      validationPredicate.let {
        if (it != null) it.invoke(item)
        else item
      }
    }.let {
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
  @Suppress("unused", "HardCodedStringLiteral")
  fun validation(predicate: (T) -> T?): ViewModelStateList<T> {
    val oldPredicate = validationPredicate

    validationPredicate = if (oldPredicate != null) {
      { item ->
        oldPredicate.invoke(item)?.let {
          predicate(item)
        }
      }
    }
    else {
      { item -> predicate(item) }
    }

    return this
  }

  // TODO: unit test this
  fun unique(): ViewModelStateList<T> {
    return validation { item ->
      ifElse(condition = values.contains(item), onTrue = null, onFalse = item)
    }
  }
}