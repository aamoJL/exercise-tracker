package com.aamo.exercisetracker.features.routine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.borderlessTextFieldColors
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOrder
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import kotlinx.serialization.Serializable
import java.util.Calendar

@Serializable data class RoutineFormScreen(val id: Int)
private data class DaySelection(val day: Day, var selected: Boolean)

fun NavGraphBuilder.routineFormScreen(onBack: () -> Unit, onSave: () -> Unit) {
  composable<RoutineFormScreen> { navStack ->
    val args: RoutineFormScreen = navStack.toRoute()
    RoutineFormScreen(
      id = args.id, onBack = onBack, onSave = onSave
    )
  }
}

fun NavController.toRoutineFormScreen(id: Int) {
  this.navigate(RoutineFormScreen(id))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineFormScreen(id: Int, onBack: () -> Unit, onSave: () -> Unit) {
  val focusManager = LocalFocusManager.current

  var routineName by remember { mutableStateOf(String.EMPTY) }
  var restMinutes by remember { mutableStateOf(String.EMPTY) }
  var daySelections = remember {
    mutableStateListOf<DaySelection>().apply {
      addAll(
        Calendar.getInstance().getLocalDayOrder().map { DaySelection(day = it, selected = false) })
    }
  }

  Scaffold(topBar = {
    TopAppBar(
      title = { Text(text = if (id == 0) "New Routine" else "Edit Routine") },
      navigationIcon = { BackNavigationIconButton(onBack = onBack) },
      actions = {
        IconButton(onClick = onSave) {
          Icon(
            imageVector = Icons.Filled.Done, contentDescription = "Save routine"
          )
        }
      })
  }, modifier = Modifier.imePadding()) { innerPadding ->
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .padding(innerPadding)
        .padding(8.dp)
    ) {
      TextField(
        value = routineName,
        label = { Text("Name") },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        onValueChange = { routineName = it },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth()
      )
      TextField(
        value = restMinutes.toString(),
        label = { Text("Rest time") },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        onValueChange = { if (it.isDigitsOnly()) restMinutes = it },
        suffix = { Text("minutes") },
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onAny = {
          focusManager.clearFocus()
        }),
        modifier = Modifier
          .fillMaxWidth()
          .onFocusChanged { state ->
            if (!state.hasFocus && !restMinutes.toIntOrNull()
                .let { value -> value != null && value > 0 }) {
              restMinutes = "1"
            }
          })
      Spacer(modifier = Modifier.height(8.dp))
      Card {
        Column(modifier = Modifier.padding(8.dp)) {
          Text(
            text = "Days",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp)
          )
          LazyRow(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp)
          ) {
            itemsIndexed(daySelections) { i, selection ->
              IconToggleButton(
                colors = IconButtonDefaults.outlinedIconToggleButtonColors(
                  checkedContainerColor = MaterialTheme.colorScheme.inversePrimary,
                  checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                  contentColor = MaterialTheme.colorScheme.outline,
                ), checked = selection.selected, onCheckedChange = {
                  daySelections[i] = selection.copy(selected = !selection.selected)
                }, modifier = Modifier
              ) {
                Text(stringResource(selection.day.nameResourceKey).take(2).toString())
              }
            }
          }
        }
      }
    }
  }
}