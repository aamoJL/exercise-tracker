package com.aamo.exercisetracker.test_utility.ui.extensions

import androidx.compose.ui.semantics.SemanticsProperties.EditableText
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.aamo.exercisetracker.MainActivity
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

fun SemanticsNodeInteraction.assertEditableText(text: String): SemanticsNodeInteraction {
  assertEquals(text, this.fetchSemanticsNode().config[EditableText].text)

  return this
}

fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.onNodeWithEditableText(
  text: String
): SemanticsNodeInteraction {
  return this.onNode(hasEditableText(text))
}

fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.onAllNodesWithEditableText(
  text: String
): SemanticsNodeInteractionCollection {
  return this.onAllNodes(hasEditableText(text))
}

@Suppress("HardCodedStringLiteral")
fun hasEditableText(text: String): SemanticsMatcher {
  return SemanticsMatcher(description = "Has editable text", matcher = {
    it.config.getOrNull(EditableText)?.text?.equals(text) ?: false
  })
}