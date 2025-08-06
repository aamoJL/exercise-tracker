package com.aamo.exercisetracker.utility.extensions.form

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import kotlin.math.min

val VisualTransformation.Companion.HideZero: VisualTransformation
  get() = object : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
      val formatted = if (text.text == "0") String.EMPTY else text.text

      return TransformedText(
        text = AnnotatedString(formatted), offsetMapping = object : OffsetMapping {
          override fun originalToTransformed(offset: Int): Int {
            return min(formatted.length, offset)
          }

          override fun transformedToOriginal(offset: Int): Int {
            return min(formatted.length, offset)
          }
        })
    }
  }