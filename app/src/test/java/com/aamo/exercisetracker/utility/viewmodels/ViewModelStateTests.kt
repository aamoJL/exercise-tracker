package com.aamo.exercisetracker.utility.viewmodels

import com.aamo.exercisetracker.utility.extensions.general.ifElse
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ViewModelStateTests {
  @Test
  fun `update test`() {
    ViewModelState(0).apply {
      update(1)
    }.also {
      assertEquals(1, it.value)
    }
  }

  @Test
  fun `onChange changes test`() {
    var value = false
    ViewModelState(0).apply {
      onChange { value = true }
      update(1)
    }

    assertTrue(value)
  }

  @Test
  fun `onChange no changes test`() {
    var value = false

    ViewModelState(0).apply {
      onChange { value = true }
      update(0)
    }.also {
      assertFalse(value)
    }
  }

  @Test
  fun `validation valid test`() {
    ViewModelState(0).apply {
      validation { ifElse(condition = it > 2, ifTrue = it, ifFalse = null) }
      update(3)
    }.also {
      assertEquals(3, it.value)
    }
  }

  @Test
  fun `validation invalid test`() {
    ViewModelState(0).apply {
      validation { ifElse(condition = it > 2, ifTrue = it, ifFalse = null) }
      update(1)
    }.also {
      assertEquals(0, it.value)
    }
  }
}