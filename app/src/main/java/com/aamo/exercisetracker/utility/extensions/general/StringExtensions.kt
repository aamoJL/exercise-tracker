package com.aamo.exercisetracker.utility.extensions.general

import java.util.regex.Pattern

@Suppress("SameReturnValue") val String.Companion.EMPTY: String get() = ""

/**
 * Returns a string having leading character of the given chars removed.
 */
fun String.trimFirst(vararg chars: Char): String {
  return this.firstOrNull()?.let {
    if (chars.contains(it)) this.drop(1)
    else this
  } ?: this
}

fun String.isValidDecimalNumberString(): Boolean {
  @Suppress("HardCodedStringLiteral") return Pattern.matches("^-?\\d*\\.?\\d*", this)
}

fun String.isValidIntegerString(): Boolean {
  @Suppress("HardCodedStringLiteral") return Pattern.matches("^-?\\d*", this)
}