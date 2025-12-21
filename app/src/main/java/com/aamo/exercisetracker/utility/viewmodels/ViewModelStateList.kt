package com.aamo.exercisetracker.utility.viewmodels

import androidx.compose.runtime.mutableStateListOf
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.extensions.general.onNotNull
import com.aamo.exercisetracker.utility.extensions.general.onTrue
import java.util.Collections

class ViewModelStateList<T>(items: List<T> = emptyList()) {
  private var validationPredicate: ((T) -> T?)? = null
  private var onChange: (() -> Unit)? = null

  private val _values = mutableStateListOf<T>().apply { addAll(items) }
  val values: List<T> = _values

  fun add(vararg items: T) {
    var changed = false

    items.forEach { item ->
      validationPredicate.let {
        if (it != null) it.invoke(item)
        else item
      }.onNotNull { item ->
        _values.add(item).onTrue {
          changed = true
        }
      }
    }

    if (changed) {
      onChange?.invoke()
    }
  }

  fun remove(vararg items: T) {
    _values.removeAll(items.toSet()).onTrue {
      onChange?.invoke()
    }
  }

  fun replaceAt(index: Int, item: T) {
    val validation = validationPredicate
    val value = if (validation != null) validation(item) else item

    if (value != null) {
      _values[index] = item
      onChange?.invoke()
    }
  }

  fun swapAt(indexA: Int, indexB: Int) {
    if (indexA == indexB) return

    Collections.swap(_values, indexA, indexB)
    onChange?.invoke()
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

  fun unique(): ViewModelStateList<T> {
    return validation { item ->
      ifElse(condition = _values.contains(item), ifTrue = { null }, ifFalse = { item })
    }
  }
}