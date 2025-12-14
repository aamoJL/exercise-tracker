package com.aamo.exercisetracker.test_utility.ui.extensions

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.yield

suspend fun SemanticsNodeInteraction.waitForDisplayed(): SemanticsNodeInteraction {
  while (this.isNotDisplayed()) yield()

  return this
}

suspend fun SemanticsNodeInteraction.waitForNotDisplayed(): SemanticsNodeInteraction {
  while (this.isDisplayed()) yield()

  return this
}

fun SemanticsNodeInteraction.performClickWithKeyboard(): SemanticsNodeInteraction {
  @OptIn(ExperimentalTestApi::class) return tryPerformAccessibilityChecks().requestFocus()
    .performKeyInput {
      keyDown(Key.Enter)
      keyUp(Key.Enter)
    }
}

fun SemanticsNodeInteraction.assertEditableText(text: String): SemanticsNodeInteraction {
  assertEquals(text, this.fetchSemanticsNode().config[SemanticsProperties.EditableText].text)

  return this
}