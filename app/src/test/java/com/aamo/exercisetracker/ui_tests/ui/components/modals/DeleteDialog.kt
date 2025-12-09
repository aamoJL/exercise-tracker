package com.aamo.exercisetracker.ui_tests.ui.components.modals

import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aamo.exercisetracker.MainActivity
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.ui.components.modals.DeleteDialog
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("HardCodedStringLiteral")
@RunWith(RobolectricTestRunner::class)
class DeleteDialog {
  @get:Rule val rule = createAndroidComposeRule<MainActivity>()

  @Test
  fun `Dialog visibility with open true`() {
    val title = "Title"
    rule.activity.setContent {
      DeleteDialog(open = true, title = title, onConfirm = {}, onDismiss = {})
    }

    rule.onNodeWithText(title).assertExists()
  }

  @Test
  fun `Dialog invisibility with open false`() {
    val title = "Title"
    rule.activity.setContent {
      DeleteDialog(open = false, title = title, onConfirm = {}, onDismiss = {})
    }

    rule.onNodeWithText(title).assertDoesNotExist()
  }

  @Test
  fun `Title text display`() {
    val title = "Delete Item"
    rule.activity.setContent {
      DeleteDialog(open = true, title = title, onConfirm = {}, onDismiss = {})
    }

    rule.onNodeWithText(title).assertExists()
  }

  @Test
  fun `Empty title text`() {
    val title = String.EMPTY
    rule.activity.setContent {
      DeleteDialog(open = true, title = title, onConfirm = {}, onDismiss = {})
    }

    rule.onNodeWithText(rule.activity.getString(R.string.btn_delete)).assertExists()
    rule.onNodeWithText(title).assertExists()
  }

  @Test
  fun `Confirm button click invokes onConfirm`() {
    var confirmed = false
    rule.activity.setContent {
      DeleteDialog(open = true, title = "Test", onConfirm = { confirmed = true }, onDismiss = {})
    }
    rule.onNodeWithText(rule.activity.getString(R.string.btn_delete)).performClick()
    assertTrue(confirmed)
  }

  @Test
  fun `Cancel button click invokes onDismiss`() {
    var dismissed = false
    rule.activity.setContent {
      DeleteDialog(open = true, title = "Test", onConfirm = {}, onDismiss = { dismissed = true })
    }
    rule.onNodeWithText(rule.activity.getString(R.string.btn_cancel)).performClick()
    assertTrue(dismissed)
  }

  @Test
  fun `Cancel button text verification`() {
    rule.activity.setContent {
      DeleteDialog(open = true, title = "Test", onConfirm = {}, onDismiss = {})
    }
    rule.onNodeWithText(rule.activity.getString(R.string.btn_cancel)).assertExists()
  }

  @Test
  fun `Recomposition with open state change`() {
    var open by mutableStateOf(false)
    val title = "Title"

    rule.activity.setContent {
      DeleteDialog(open = open, title = title, onConfirm = {}, onDismiss = {})
    }

    rule.onNodeWithText(title).assertDoesNotExist()

    open = true
    rule.waitForIdle()

    rule.onNodeWithText(title).assertExists()

    open = false
    rule.waitForIdle()

    rule.onNodeWithText(title).assertDoesNotExist()
  }

  @Test
  fun `Recomposition with title change`() {
    val initialTitle = "Initial Title"
    val updatedTitle = "Updated Title"
    var title by mutableStateOf(initialTitle)
    rule.activity.setContent {
      DeleteDialog(open = true, title = title, onConfirm = {}, onDismiss = {})
    }

    rule.onNodeWithText(initialTitle).assertExists()

    title = updatedTitle

    rule.waitForIdle()

    rule.onNodeWithText(initialTitle).assertDoesNotExist()
    rule.onNodeWithText(updatedTitle).assertExists()
  }

  @Test
  fun `onConfirm lambda does not invoke onDismiss`() {
    var onConfirmCalled = false
    rule.activity.setContent {
      DeleteDialog(
        open = true,
        title = "Test",
        onConfirm = { onConfirmCalled = true },
        onDismiss = { })
    }

    rule.onNodeWithText(rule.activity.getString(R.string.btn_cancel)).performClick()

    assertFalse(onConfirmCalled)
  }

  @Test
  fun `onDismiss lambda does not invoke onConfirm`() {
    var onDismissCalled = false
    rule.activity.setContent {
      DeleteDialog(
        open = true,
        title = "Test",
        onConfirm = { },
        onDismiss = { onDismissCalled = true })
    }

    rule.onNodeWithText(rule.activity.getString(R.string.btn_delete)).performClick()

    assertFalse(onDismissCalled)
  }
}