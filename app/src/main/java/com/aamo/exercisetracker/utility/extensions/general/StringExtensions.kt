package com.aamo.exercisetracker.utility.extensions.general

@Suppress("SameReturnValue") val String.Companion.EMPTY: String get() = ""

fun String.trimFirst(char: Char): String {
  return if (this.startsWith(char)) this.drop(1) else this
}