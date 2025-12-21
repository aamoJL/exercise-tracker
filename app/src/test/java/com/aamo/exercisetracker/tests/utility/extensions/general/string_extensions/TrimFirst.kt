package com.aamo.exercisetracker.tests.utility.extensions.general.string_extensions

import com.aamo.exercisetracker.utility.extensions.general.trimFirst
import org.junit.Test

class TrimFirst {
  @Test
  fun trimFirst() {
    val string = "abc"

    assert(string.trimFirst('a') == "bc")
    assert(string.trimFirst('c') == "abc")
    assert(string.trimFirst('a', 'b') == "bc")
  }
}