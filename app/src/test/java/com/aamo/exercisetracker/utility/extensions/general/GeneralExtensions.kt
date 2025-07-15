@file:Suppress("KotlinConstantConditions")

package com.aamo.exercisetracker.utility.extensions.general

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class GeneralExtensionsTests {
  @Test
  fun `applyIf test`() {
    assertEquals(1, applyIf(condition = true, value = { 1 }))
    assertEquals(null, applyIf(condition = false, value = { 1 }))
  }

  @Test
  fun `letIf test`() {
    assertEquals(1, 0.letIf(true) { 1 })
    assertEquals(0, 0.letIf(false) { 1 })
  }

  @Test
  fun `onFalse test`() {
    var value = false
    value.onFalse { value = true }.also {
      assertEquals(true, value)
    }

    true.onFalse { fail() }
  }

  @Test
  fun `onTrue test`() {
    var value = true
    value.onTrue { value = false }.also {
      assertEquals(false, value)
    }

    false.onTrue { fail() }
  }

  @Test
  fun `onNotNull test`() {
    var value: Boolean? = true
    value.onNotNull { value = false }.also {
      assertEquals(false, value)
    }

    null.onNotNull { fail() }
  }

  @Test
  fun `onNull test`() {
    var value: Boolean? = null
    value.onNull { value = false }.also {
      assertEquals(false, value)
    }

    true.onNull { fail() }
  }

  @Test
  fun `ifElse test`() {
    assertTrue(ifElse(condition = false, ifTrue = { false.also { fail() } }, ifFalse = { true }))
    assertFalse(ifElse(condition = true, ifTrue = { false }, ifFalse = { true.also { fail() } }))
  }
}