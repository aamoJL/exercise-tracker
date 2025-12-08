package com.aamo.exercisetracker.tests.utility.viewmodels

import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.viewmodels.ViewModelStateList
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ViewModelStateList {
  @Test
  fun add() {
    var changed = false
    ViewModelStateList<Int>().apply {
      onChange { changed = true }
      add(1)
    }.also {
      assertEquals(1, it.values.size)
      assertTrue(it.values.contains(1))
      assertTrue(changed)
    }
  }

  @Test
  fun remove() {
    var changed = false
    ViewModelStateList<Int>().apply {
      add(1, 2)
      onChange { changed = true }
      remove(1)
    }.also {
      assertEquals(1, it.values.size)
      assertTrue(it.values.contains(2))
      assertTrue(changed)
    }
  }

  @Test
  fun clear() {
    var changed = false
    ViewModelStateList<Int>().apply {
      add(1, 2, 3)
      onChange { changed = true }
      clear()
    }.also {
      assertEquals(0, it.values.size)
      assertTrue(changed)
    }
  }

  @Test
  fun `validation valid`() {
    ViewModelStateList<Int>().apply {
      validation { ifElse(condition = it > 2, ifTrue = { it }, ifFalse = { null }) }
      add(3)
    }.also {
      assertEquals(1, it.values.size)
      assertTrue(it.values.contains(3))
    }
  }

  @Test
  fun `validation invalid`() {
    ViewModelStateList<Int>().apply {
      validation { ifElse(condition = it > 2, ifTrue = { it }, ifFalse = { null }) }
      add(1)
    }.also {
      assertEquals(0, it.values.size)
    }
  }

  @Test
  fun unique() {
    ViewModelStateList<Int>().apply {
      unique()
      add(3, 3, 3)
    }.also {
      assertEquals(1, it.values.size)
      assertTrue(it.values.contains(3))
    }
  }

  @Test
  fun `multiple validations`() {
    ViewModelStateList<Int>().apply {
      validation { ifElse(condition = it > 2, ifTrue = { it }, ifFalse = { null }) }
      unique()
      add(3, 3, 3, 1, 1)
    }.also {
      assertEquals(1, it.values.size)
      assertTrue(it.values.contains(3))
    }
  }

  @Test
  fun `replaceAt valid`() {
    var changed = false
    ViewModelStateList<Int>().apply {
      add(6, 3)
      onChange { changed = true }
      replaceAt(index = 0, 1)
    }.also {
      assertEquals(listOf(1, 3), it.values)
      assertTrue(changed)
    }
  }

  @Test
  fun `replaceAt invalid`() {
    var changed = false
    ViewModelStateList<Int>().apply {
      validation { if (it == 3) null else it }
      add(6, 2)
      onChange { changed = true }
      replaceAt(index = 1, 3)
    }.also {
      assertEquals(listOf(6, 2), it.values)
      assertFalse(changed)
    }
  }

  @Test
  fun swapAt() {
    var changed = false
    ViewModelStateList<Int>().apply {
      add(6, 2)
      onChange { changed = true }
      swapAt(0, 1)
    }.also {
      assertEquals(listOf(2, 6), it.values)
      assertTrue(changed)
    }
  }
}