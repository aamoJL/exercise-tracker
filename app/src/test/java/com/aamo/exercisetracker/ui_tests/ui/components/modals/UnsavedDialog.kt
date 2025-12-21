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
import com.aamo.exercisetracker.ui.components.modals.UnsavedDialog
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UnsavedDialog {
  @get:Rule val rule = createAndroidComposeRule<MainActivity>()

  @Test
  fun `Dialog visibility when open is true`() {
    rule.activity.setContent {
      UnsavedDialog(open = true, onConfirm = {}, onDismiss = {})
    }

    rule.onNodeWithText(rule.activity.getString(R.string.dialog_title_unsaved_changes))
      .assertExists()
  }

  @Test
  fun `Dialog not in composition when open is false`() {
    rule.activity.setContent {
      UnsavedDialog(open = false, onConfirm = {}, onDismiss = {})
    }

    rule.onNodeWithText(rule.activity.getString(R.string.dialog_title_unsaved_changes))
      .assertDoesNotExist()
  }

  @Test
  fun `Confirm button click invokes onConfirm lambda`() {
    var confirmed = false
    rule.activity.setContent {
      UnsavedDialog(open = true, onConfirm = { confirmed = true }, onDismiss = {})
    }

    rule.onNodeWithText(rule.activity.getString(R.string.btn_yes)).performClick()

    assertTrue(confirmed)
  }

  @Test
  fun `Cancel button click invokes onDismiss lambda`() {
    var dismissed = false
    rule.activity.setContent {
      UnsavedDialog(open = true, onConfirm = {}, onDismiss = { dismissed = true })
    }

    rule.onNodeWithText(rule.activity.getString(R.string.btn_cancel)).performClick()

    assertTrue(dismissed)
  }

  @Test
  fun `Dialog content verification`() {
    rule.activity.setContent {
      UnsavedDialog(open = true, onConfirm = {}, onDismiss = {})
    }

    rule.onNodeWithText(rule.activity.getString(R.string.dialog_title_unsaved_changes))
      .assertExists()
    rule.onNodeWithText(rule.activity.getString(R.string.dialog_text_unsaved_changes))
      .assertExists()
  }

  @Test
  fun `Confirm button text verification`() {
    rule.activity.setContent {
      UnsavedDialog(open = true, onConfirm = {}, onDismiss = {})
    }

    rule.onNodeWithText(rule.activity.getString(R.string.btn_yes)).assertExists()
  }

  @Test
  fun `Dismiss button text verification`() {
    rule.activity.setContent {
      UnsavedDialog(open = true, onConfirm = {}, onDismiss = {})
    }

    rule.onNodeWithText(rule.activity.getString(R.string.btn_cancel)).assertExists()
  }

  @Test
  fun `Recomposition with open state change`() {
    var open by mutableStateOf(false)

    rule.activity.setContent {
      UnsavedDialog(open = open, onConfirm = {}, onDismiss = {})
    }

    rule.onNodeWithText(rule.activity.getString(R.string.dialog_title_unsaved_changes))
      .assertDoesNotExist()

    open = true
    rule.waitForIdle()

    rule.onNodeWithText(rule.activity.getString(R.string.dialog_title_unsaved_changes))
      .assertExists()

    open = false
    rule.waitForIdle()

    rule.onNodeWithText(rule.activity.getString(R.string.dialog_title_unsaved_changes))
      .assertDoesNotExist()
  }

  @Test
  fun `No multiple invocations on rapid clicks`() {
    val openState = mutableStateOf(true)

    rule.activity.setContent {
      UnsavedDialog(open = openState.value, onConfirm = {
        openState.value = false
      }, onDismiss = {})
    }

    rule.onNodeWithText(rule.activity.getString(R.string.btn_yes)).performClick()
    rule.onNodeWithText(rule.activity.getString(R.string.btn_yes)).assertDoesNotExist()
  }

  @Test
  fun `onDismiss lambda is not invoked on confirm`() {
    var onDismissCalled = false

    rule.activity.setContent {
      UnsavedDialog(open = true, onConfirm = {}, onDismiss = { onDismissCalled = true })
    }

    rule.onNodeWithText(rule.activity.getString(R.string.btn_yes)).performClick()

    assertFalse(onDismissCalled)
  }

  @Test
  fun `onConfirm lambda is not invoked on dismiss`() {
    var onConfirmCalled = false

    rule.activity.setContent {
      UnsavedDialog(open = true, onConfirm = { onConfirmCalled = true }, onDismiss = {})
    }

    rule.onNodeWithText(rule.activity.getString(R.string.btn_cancel)).performClick()

    assertFalse(onConfirmCalled)
  }
}