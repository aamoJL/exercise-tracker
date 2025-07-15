package com.aamo.exercisetracker.utility.viewmodels

import com.aamo.exercisetracker.utility.extensions.general.ifElse
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ViewModelStateListTests {
  @Test
  fun `add test`() {
    ViewModelStateList<Int>().apply {
      add(1)
    }.also {
      assertEquals(1, it.values.size)
      assertTrue(it.values.contains(1))
    }
  }

  @Test
  fun `remove test`() {
    ViewModelStateList<Int>().apply {
      add(1, 2)
      remove(1)
    }.also {
      assertEquals(1, it.values.size)
      assertTrue(it.values.contains(2))
    }
  }

  @Test
  fun `clear test`() {
    ViewModelStateList<Int>().apply {
      add(1, 2, 3)
      clear()
    }.also {
      assertEquals(0, it.values.size)
    }
  }

  @Test
  fun `onChange test`() {
    var changed = false

    ViewModelStateList<Int>().apply {
      onChange { changed = true }
      add(1)
    }.also {
      assertTrue(changed)
    }
  }

  @Test
  fun `validation valid test`() {
    ViewModelStateList<Int>().apply {
      validation { ifElse(condition = it > 2, ifTrue = { it }, ifFalse = { null }) }
      add(3)
    }.also {
      assertEquals(1, it.values.size)
      assertTrue(it.values.contains(3))
    }
  }

  @Test
  fun `validation invalid test`() {
    ViewModelStateList<Int>().apply {
      validation { ifElse(condition = it > 2, ifTrue = { it }, ifFalse = { null }) }
      add(1)
    }.also {
      assertEquals(0, it.values.size)
    }
  }

  @Test
  fun `unique test`() {
    ViewModelStateList<Int>().apply {
      unique()
      add(3, 3, 3)
    }.also {
      assertEquals(1, it.values.size)
      assertTrue(it.values.contains(3))
    }
  }

  @Test
  fun `multiple validations test`() {
    ViewModelStateList<Int>().apply {
      validation { ifElse(condition = it > 2, ifTrue = { it }, ifFalse = { null }) }
      unique()
      add(3, 3, 3, 1, 1)
    }.also {
      assertEquals(1, it.values.size)
      assertTrue(it.values.contains(3))
    }
  }
}