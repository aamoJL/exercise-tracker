package com.aamo.exercisetracker.ui.components

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

enum class SheetVisibility {
  VISIBLE,
  HIDDEN,
  CLOSING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GesturelessModalBottomSheet(
  show: Boolean,
  onDismissRequest: () -> Unit,
  content: @Composable ColumnScope.() -> Unit,
) {
  var visibility by remember { mutableStateOf(SheetVisibility.HIDDEN) }
  val sheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true, confirmValueChange = {
      /* Prevents closing by pressing outside the sheet */
      if (it == SheetValue.Hidden) {
        visibility == SheetVisibility.CLOSING
      }
      else true
    })

  // Set visibility
  LaunchedEffect(show) {
    if (show) {
      visibility = SheetVisibility.VISIBLE
    }
    else if (visibility != SheetVisibility.HIDDEN) {
      visibility = SheetVisibility.CLOSING
    }
  }

  // Hide with animation when closing
  LaunchedEffect(visibility) {
    if (visibility == SheetVisibility.CLOSING) {
      sheetState.hide()
      visibility = SheetVisibility.HIDDEN
    }
  }

  if (visibility != SheetVisibility.HIDDEN) {
    ModalBottomSheet(
      sheetState = sheetState, onDismissRequest = onDismissRequest, dragHandle = null
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight(.8f)
          .pointerInput(Unit) {
            detectVerticalDragGestures(onVerticalDrag = { change, _ -> change.consume() })
          }) {
        content()
      }
    }
  }
}