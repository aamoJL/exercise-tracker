package com.aamo.exercisetracker.features.routine

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.aamo.exercisetracker.database.RoutineDatabase
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.LoadingIconButton
import com.aamo.exercisetracker.ui.components.UnsavedDialog
import com.aamo.exercisetracker.ui.components.borderlessTextFieldColors
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOrder
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import com.aamo.exercisetracker.utility.viewmodels.SavingState
import com.aamo.exercisetracker.utility.viewmodels.ViewModelState
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Calendar
import kotlin.time.Duration.Companion.minutes

@Serializable
data class RoutineFormScreen(val id: Long)

class RoutineFormViewModel(context: Context, routineId: Long) : ViewModel() {
  class UiState(onModelChanged: () -> Unit) {
    val routine = ViewModelState(
      Routine(name = String.EMPTY, restDuration = 1.minutes)
    ).onChange { onModelChanged() }
    val schedule = ViewModelState(RoutineSchedule(routineId = 0L)).onChange { onModelChanged() }
    var savingState by mutableStateOf(SavingState(SavingState.State.NONE))
    var deleted by mutableStateOf(false)
  }

  private val database: RoutineDatabase = RoutineDatabase.getDatabase(context)

  val uiState = UiState(onModelChanged = { onModelChanged() })

  init {
    viewModelScope.launch {
      database.routineDao().getRoutineWithSchedule(routineId)?.let { rws ->
        uiState.routine.update(rws.routine)
        uiState.schedule.update(rws.schedule ?: RoutineSchedule(routineId = rws.routine.id))
        uiState.savingState = SavingState(canSave = canSave())
      }
    }
  }

  /**
   * Saves the routine to the database
   */
  fun save() {
    if (!canSave()) return

    val routine = uiState.routine.value
    val schedule = uiState.schedule.value

    uiState.apply { savingState = savingState.getAsSaving() }

    viewModelScope.launch {
      database.routineDao().runCatching {
        checkNotNull(upsertAndGet(RoutineWithSchedule(routine = routine, schedule = schedule)))
      }.onSuccess { result ->
        uiState.routine.update(result.routine)
        uiState.schedule.update(result.schedule ?: schedule)
        uiState.apply { savingState = savingState.getAsSaved(canSave()) }
      }.onFailure { error ->
        uiState.apply { savingState = savingState.getAsError(error = Error(error)) }
      }
    }
  }

  /**
   * Deletes the routine from the database
   */
  fun delete() {
    if (uiState.routine.value.id == 0L) return

    viewModelScope.launch {
      database.routineDao().runCatching {
        delete(uiState.routine.value)
      }.onSuccess {
        uiState.deleted = true
      }
    }
  }

  private fun canSave(): Boolean {
    if (uiState.savingState.state == SavingState.State.SAVING) return false
    return uiState.routine.value.name.isNotEmpty()
  }

  private fun onModelChanged() {
    uiState.savingState = uiState.savingState.copy(
      unsavedChanges = true, canSave = canSave()
    )
  }
}

fun NavGraphBuilder.routineFormScreen(
  onBack: () -> Unit, onSuccess: (id: Long) -> Unit, onDelete: () -> Unit
) {
  composable<RoutineFormScreen> { navStack ->
    val (id) = navStack.toRoute<RoutineFormScreen>()
    val context = LocalContext.current.applicationContext
    val viewmodel: RoutineFormViewModel = viewModel(factory = viewModelFactory {
      initializer { RoutineFormViewModel(context = context, routineId = id) }
    })
    val uiState = viewmodel.uiState

    LaunchedEffect(uiState.deleted) {
      if (uiState.deleted) {
        onDelete()
      }
    }

    LaunchedEffect(uiState.savingState.state) {
      if (uiState.savingState.state == SavingState.State.SAVED) {
        onSuccess(uiState.routine.value.id)
      }
    }

    RoutineFormScreen(
      uiState = uiState,
      onBack = onBack,
      onSave = { viewmodel.save() },
      onDelete = { viewmodel.delete() })
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineFormScreen(
  uiState: RoutineFormViewModel.UiState,
  onBack: () -> Unit,
  onSave: () -> Unit,
  onDelete: () -> Unit,
) {
  val focusManager = LocalFocusManager.current
  val days = remember { Calendar.getInstance().getLocalDayOrder() }
  val routine = remember(uiState.routine) { uiState.routine }
  val schedule = remember(uiState.schedule) { uiState.schedule }
  val unsavedChanges =
    remember(uiState.savingState.unsavedChanges) { uiState.savingState.unsavedChanges }
  val isNew = remember(routine.value.id) { routine.value.id == 0L }

  var openDeleteDialog by remember { mutableStateOf(false) }
  var openUnsavedDialog by remember { mutableStateOf(false) }

  if (openUnsavedDialog) {
    UnsavedDialog(
      onDismiss = { openUnsavedDialog = false },
      onConfirm = {
        openUnsavedDialog = false
        onBack()
      },
    )
  }
  if (openDeleteDialog) {
    DeleteDialog(onDismiss = { openDeleteDialog = false }, onConfirm = {
      openDeleteDialog = false
      onDelete()
    })
  }

  BackHandler(enabled = unsavedChanges) {
    openUnsavedDialog = true
  }

  Scaffold(topBar = {
    TopAppBar(
      title = { Text(text = if (isNew) "New Routine" else "Edit Routine") },
      navigationIcon = {
        BackNavigationIconButton(onBack = {
          if (unsavedChanges) openUnsavedDialog = true else onBack()
        })
      },
      actions = {
        if (!isNew) {
          IconButton(onClick = { openDeleteDialog = true }) {
            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete routine")
          }
        }
        LoadingIconButton(
          onClick = onSave,
          isLoading = uiState.savingState.state == SavingState.State.SAVING,
          enabled = uiState.savingState.canSave
        ) {
          Icon(imageVector = Icons.Filled.Done, contentDescription = "Save routine")
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
        value = routine.value.name,
        label = { Text("Name") },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        onValueChange = { routine.apply { update(value.copy(name = it)) } },
        keyboardOptions = KeyboardOptions(
          imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Sentences
        ),
        modifier = Modifier.fillMaxWidth()
      )
      TextField(
        value = if (routine.value.restDuration.inWholeMinutes == 0L) String.EMPTY else routine.value.restDuration.inWholeMinutes.toString(),
        label = { Text("Rest duration") },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        onValueChange = {
          if (it.isEmpty()) {
            routine.apply { update(value.copy(restDuration = 0.minutes)) }
          }
          else if (it.isDigitsOnly()) {
            // int can have max 9 digits
            routine.apply {
              update(
                value.copy(
                  restDuration = it.take(9).toInt().minutes
                )
              )
            }
          }
        },
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
            if (!state.isFocused && routine.value.restDuration <= 0.minutes) {
              routine.apply { update(value.copy(restDuration = 1.minutes)) }
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
            items(days) { day ->
              val isSelected by remember(schedule.value) {
                mutableStateOf(schedule.value.isDaySelected(day.getDayNumber()))
              }

              IconToggleButton(
                colors = IconButtonDefaults.outlinedIconToggleButtonColors(
                  checkedContainerColor = MaterialTheme.colorScheme.inversePrimary,
                  checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                  contentColor = MaterialTheme.colorScheme.outline,
                ), checked = isSelected, onCheckedChange = { selection ->
                  schedule.update(schedule.value.setDaySelection(day, selection))
                }, modifier = Modifier
              ) {
                Text(stringResource(day.nameResourceKey).take(2).toString())
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DeleteDialog(
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) {
  AlertDialog(
    title = { Text(text = "Delete routine?") },
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(
        onClick = onConfirm,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
      ) {
        Text(text = "Delete")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = "Cancel")
      }
    },
  )
}

private fun RoutineSchedule.setDaySelection(day: Day, value: Boolean): RoutineSchedule {
  return when (day) {
    Day.SUNDAY -> this.copy(sunday = value)
    Day.MONDAY -> this.copy(monday = value)
    Day.TUESDAY -> this.copy(tuesday = value)
    Day.WEDNESDAY -> this.copy(wednesday = value)
    Day.THURSDAY -> this.copy(thursday = value)
    Day.FRIDAY -> this.copy(friday = value)
    Day.SATURDAY -> this.copy(saturday = value)
  }
}