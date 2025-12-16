package com.aamo.exercisetracker.tests.utility.extensions.form

import com.aamo.exercisetracker.utility.extensions.form.getNewUUID
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import java.util.UUID

class FormExtensions {
  @Test
  fun getNewUUID() {
    val used = listOf(UUID.randomUUID())
    val actual = getNewUUID(used = used)

    assertNotNull(actual)
    assertFalse(used.contains(actual))
  }
}