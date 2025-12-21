@file:Suppress("HardCodedStringLiteral")

package com.aamo.exercisetracker.ui_tests.ui.components.inputs.number_field.int_number_field.int_field_validator

import com.aamo.exercisetracker.ui.components.inputs.number_field.IntFieldValidator
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class ValidateText {
  @Test
  fun `validate valid text`() {
    val inputOutputs = listOf(
      "100" to ("100" to 100),
      "-100" to ("-100" to -100),
      "2147483647" to ("2147483647" to Int.MAX_VALUE),
      "-2147483647" to ("-2147483647" to -Int.MAX_VALUE),
      "-2147483648" to ("-2147483648" to Int.MIN_VALUE),
      String.EMPTY to ("0" to 0),
      "0" to ("0" to 0),
      "000" to ("0" to 0),
      "00100" to ("100" to 100),
      "0-" to ("-" to 0),
      "00-" to ("-" to 0),
      "-" to ("-" to 0),
    )

    inputOutputs.forEach { (input, output) ->
      var value: Int? = null
      var text: String? = null

      IntFieldValidator.onValid(text = input) { v, t -> value = v; text = t }
      assertEquals(output.first, text)
      assertEquals(output.second, value)
    }
  }

  @Test
  fun `validate invalid text`() {
    val inputs = listOf(
      "1.99999",
      "-1.99999",
      "0.0",
      "000.000",
      "0.100",
      "9990282350000000000000000000000000000000",
      "-9990282350000000000000000000000000000000",
      ".",
      ".00",
      "..",
      ".-1",
      "1.0.0",
      "1 2",
      " ",
      "test",
      "12f",
      "--12",
      "-.-12",
      "1-2",
    )

    inputs.forEach { input ->
      IntFieldValidator.onValid(input) { _, _ -> fail() }
    }
  }
}