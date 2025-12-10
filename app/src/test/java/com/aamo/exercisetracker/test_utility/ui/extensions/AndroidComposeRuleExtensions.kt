package com.aamo.exercisetracker.test_utility.ui.extensions

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import kotlinx.coroutines.yield

suspend fun SemanticsNodeInteraction.waitForDisplayed(): SemanticsNodeInteraction {
  while (this.isNotDisplayed()) yield()

  return this
}

fun SemanticsNodeInteraction.performClickWithKeyboard(): SemanticsNodeInteraction {
  @OptIn(ExperimentalTestApi::class) return tryPerformAccessibilityChecks().requestFocus()
    .performKeyInput {
      keyDown(Key.Enter)
      keyUp(Key.Enter)
    }
}