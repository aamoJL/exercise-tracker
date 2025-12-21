package com.aamo.exercisetracker.ui.components.inputs.number_field

import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import com.aamo.exercisetracker.utility.extensions.general.isValidIntegerString
import com.aamo.exercisetracker.utility.extensions.general.letIf

data object IntFieldValidator : FieldValidator<Int> {
  override fun onValid(text: String, onValid: (value: Int, text: String) -> Unit) {
    val result = transformText(text = text) ?: return
    val value = getValueFromText(result) ?: return

    onValid(value, result)
  }

  override fun onValid(value: Int, onValid: (text: String) -> Unit): Boolean {
    onValid(value.toString())
    return true
  }

  private fun getValueFromText(text: String): Int? {
    if (!text.isValidIntegerString()) return null

    return when (text) {
      String.EMPTY -> 0
      "-" -> 0
      else -> text.toIntOrNull()
    }
  }

  private fun transformText(text: String): String? {
    // zeroes needs to be trimmed so the value will be valid when the text is "0-"
    val result = text.trimStart('0')

    if (getValueFromText(result) == null) return null

    return result.letIf({ it.isEmpty() }) { "0" }
  }
}