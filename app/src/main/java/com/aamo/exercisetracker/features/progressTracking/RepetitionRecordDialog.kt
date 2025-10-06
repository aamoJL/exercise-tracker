package com.aamo.exercisetracker.features.progressTracking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.ui.components.IntNumberField
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepetitionRecordDialog(
  label: String,
  value: Int,
  valueUnit: String,
  date: Date,
  onConfirm: (value: Int, date: Date) -> Unit,
  onDismiss: () -> Unit
) {
  val focusRequester = remember { FocusRequester() }
  var showDatePicker by remember { mutableStateOf(false) }
  var fieldValue by remember { mutableIntStateOf(value) }
  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date.time)

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }

  AlertDialog(title = { Text(text = label) }, text = {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      IntNumberField(
        value = fieldValue,
        onValueChange = { fieldValue = it },
        suffix = { Text(text = valueUnit) },
        modifier = Modifier.focusRequester(focusRequester)
      )
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = SimpleDateFormat.getDateInstance()
          .format(datePickerState.selectedDateMillis?.let { Date(it) } ?: date))
        IconButton(
          onClick = { showDatePicker = true }, colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.tertiary
          )
        ) {
          Icon(
            painter = painterResource(R.drawable.rounded_date_range_24),
            contentDescription = stringResource(R.string.cd_change_date)
          )
        }
      }
    }
  }, onDismissRequest = onDismiss, confirmButton = {
    TextButton(onClick = {
      onConfirm(fieldValue, datePickerState.selectedDateMillis?.let { Date(it) } ?: date)
    }) {
      Text(text = stringResource(R.string.btn_save))
    }
  }, dismissButton = {
    TextButton(onClick = onDismiss) {
      Text(text = stringResource(R.string.btn_cancel))
    }
  })

  if (showDatePicker) {
    val initDate = remember { datePickerState.selectedDateMillis }

    DatePickerDialog(onDismissRequest = {
      showDatePicker = false
      datePickerState.selectedDateMillis = initDate
    }, confirmButton = {
      TextButton(onClick = { showDatePicker = false }) {
        Text(text = stringResource(R.string.btn_select))
      }
    }, dismissButton = {
      TextButton(onClick = {
        showDatePicker = false
        datePickerState.selectedDateMillis = initDate
      }) {
        Text(text = stringResource(R.string.btn_cancel))
      }
    }) {
      DatePicker(state = datePickerState)
    }
  }
}