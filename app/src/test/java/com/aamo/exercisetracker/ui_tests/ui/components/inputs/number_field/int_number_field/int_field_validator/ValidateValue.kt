package com.aamo.exercisetracker.ui_tests.ui.components.inputs.number_field.int_number_field.int_field_validator

import com.aamo.exercisetracker.ui.components.inputs.number_field.IntFieldValidator
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateValue {
  @Test
  fun `validate valid value`() {
    val inputOutputs = listOf(
      0 to "0",
      1 to "1",
      -1 to "-1",
      Int.MAX_VALUE to "2147483647",
      -Int.MAX_VALUE to "-2147483647",
      Int.MIN_VALUE to "-2147483648",
    )

    inputOutputs.forEach { (input, output) ->
      var text: String? = null
      assertTrue(IntFieldValidator.onValid(value = input) { text = it })
      Assert.assertEquals(output, text)
    }
  }
}