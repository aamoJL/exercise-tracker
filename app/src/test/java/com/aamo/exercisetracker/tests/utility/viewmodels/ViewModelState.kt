package com.aamo.exercisetracker.tests.utility.viewmodels

import com.aamo.exercisetracker.utility.viewmodels.ViewModelState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ViewModelState {
  @Test
  fun update() {
    ViewModelState(0).apply {
      update(1)
    }.also {
      assertEquals(1, it.value)
    }
  }

  @Test
  fun `onChange changes`() {
    var value = false
    ViewModelState(0).apply {
      onChange { value = true }
      update(1)
    }

    assertTrue(value)
  }

  @Test
  fun `onChange no changes`() {
    var value = false

    ViewModelState(0).apply {
      onChange { value = true }
      update(0)
    }.also {
      assertFalse(value)
    }
  }

  @Test
  fun transformation() {
    ViewModelState(0).apply {
      transformation { it + 1 }
      update(3)
    }.also {
      assertEquals(4, it.value)
    }
  }

  @Test
  fun validation_invalid() {
    ViewModelState(0).apply {
      validation { it > 3 }
      update(3)
    }.also {
      assertEquals(0, it.value)
    }
  }

  @Test
  fun validation_valid() {
    ViewModelState(0).apply {
      validation { it > 3 }
      update(4)
    }.also {
      assertEquals(4, it.value)
    }
  }
}