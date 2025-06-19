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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.aamo.exercisetracker.features.routine.RoutineFormViewModel.RoutineFormUiState
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.LoadingIconButton
import com.aamo.exercisetracker.ui.components.UnsavedDialog
import com.aamo.exercisetracker.ui.components.borderlessTextFieldColors
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOrder
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import com.aamo.exercisetracker.utility.viewmodels.SavingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Calendar
import kotlin.time.Duration.Companion.minutes

@Serializable
data class RoutineFormScreen(val id: Long)

class RoutineFormViewModel(context: Context, routineId: Long) : ViewModel() {
  data class RoutineFormUiState(
    val routine: Routine = Routine(name = String.EMPTY, restDuration = 1.minutes),
    val schedule: RoutineSchedule = RoutineSchedule(routineId = 0L),
    val savingState: SavingState = SavingState(SavingState.State.NONE),
    val deleted: Boolean = false,
  )

  private val database: RoutineDatabase = RoutineDatabase.getDatabase(context)

  private val _uiState = MutableStateFlow(RoutineFormUiState())
  var uiState = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      database.routineDao().getRoutineWithSchedule(routineId)?.let { rws ->
        _uiState.update {
          RoutineFormUiState(
            routine = rws.routine,
            schedule = rws.schedule ?: RoutineSchedule(routineId = rws.routine.id),
            savingState = it.savingState.copy(canSave = canSave(it))
          )
        }
      }
    }
  }

  fun update(uiState: RoutineFormUiState) {
    _uiState.update {
      uiState.copy(
        savingState = uiState.savingState.copy(
          unsavedChanges = true, canSave = canSave(uiState)
        )
      )
    }
  }

  /**
   * Saves the routine to the database
   */
  fun save() {
    if (!canSave(_uiState.value)) return

    val routine = _uiState.value.routine
    val schedule = _uiState.value.schedule

    _uiState.update { it.copy(savingState = SavingState(SavingState.State.SAVING)) }

    viewModelScope.launch {
      database.routineDao().runCatching {
        checkNotNull(upsertAndGet(RoutineWithSchedule(routine = routine, schedule = schedule)))
      }.onSuccess { result ->
        _uiState.update {
          it.copy(
            routine = result.routine,
            schedule = result.schedule ?: schedule,
            savingState = SavingState(
              state = SavingState.State.SAVED, unsavedChanges = false
            )
          )
        }
      }.onFailure { error ->
        _uiState.update {
          it.copy(
            savingState = SavingState(
              state = SavingState.State.ERROR, msg = error.message ?: String.EMPTY
            )
          )
        }
      }
    }
  }

  /**
   * Deletes the routine from the database
   */
  fun delete() {
    if (_uiState.value.routine.id == 0L) return

    viewModelScope.launch {
      database.routineDao().runCatching {
        delete(_uiState.value.routine)
      }.onSuccess {
        _uiState.update { it.copy(deleted = true) }
      }
    }
  }

  private fun canSave(uiState: RoutineFormUiState): Boolean {
    if (uiState.savingState.state == SavingState.State.SAVING) return false
    return uiState.routine.name.isNotEmpty()
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
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.deleted) {
      if (uiState.deleted) {
        onDelete()
      }
    }

    LaunchedEffect(uiState.savingState.state) {
      if (uiState.savingState.state == SavingState.State.SAVED) {
        onSuccess(uiState.routine.id)
      }
    }

    RoutineFormScreen(
      uiState = uiState,
      onStateChanged = { viewmodel.update(it) },
      onBack = onBack,
      onSave = { viewmodel.save() },
      onDelete = { viewmodel.delete() })
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineFormScreen(
  uiState: RoutineFormUiState,
  onStateChanged: (RoutineFormUiState) -> Unit,
  onBack: () -> Unit,
  onSave: () -> Unit,
  onDelete: () -> Unit,
) {
  val focusManager = LocalFocusManager.current
  val days = Calendar.getInstance().getLocalDayOrder()
  val routine = uiState.routine
  val schedule = uiState.schedule

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

  BackHandler(enabled = uiState.savingState.unsavedChanges) {
    openUnsavedDialog = true
  }

  Scaffold(topBar = {
    TopAppBar(
      title = { Text(text = if (routine.id == 0L) "New Routine" else "Edit Routine") },
      navigationIcon = {
        BackNavigationIconButton(onBack = {
          if (uiState.savingState.unsavedChanges) openUnsavedDialog = true else onBack()
        })
      },
      actions = {
        if (uiState.routine.id != 0L) {
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
        value = routine.name,
        label = { Text("Name") },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        onValueChange = { onStateChanged(uiState.copy(routine = routine.copy(name = it))) },
        keyboardOptions = KeyboardOptions(
          imeAction = ImeAction.Next,
          capitalization = KeyboardCapitalization.Sentences
        ),
        modifier = Modifier.fillMaxWidth()
      )
      TextField(
        value = if (routine.restDuration.inWholeMinutes == 0L) String.EMPTY else routine.restDuration.inWholeMinutes.toString(),
        label = { Text("Rest duration") },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        onValueChange = {
          if (it.isEmpty()) onStateChanged(uiState.copy(routine = routine.copy(restDuration = 0.minutes)))
          else if (it.isDigitsOnly()) onStateChanged(
            uiState.copy(routine = routine.copy(restDuration = it.toInt().minutes))
          )
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
            if (!state.hasFocus && routine.restDuration.inWholeMinutes <= 0) {
              onStateChanged(uiState.copy(routine = routine.copy(restDuration = 1.minutes)))
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
              IconToggleButton(
                colors = IconButtonDefaults.outlinedIconToggleButtonColors(
                  checkedContainerColor = MaterialTheme.colorScheme.inversePrimary,
                  checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                  contentColor = MaterialTheme.colorScheme.outline,
                ),
                checked = isScheduleDaySelected(schedule = schedule, day = day),
                onCheckedChange = { selection ->
                  onStateChanged(
                    uiState.copy(schedule = setScheduleDaySelection(schedule, day, selection))
                  )
                },
                modifier = Modifier
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

private fun isScheduleDaySelected(schedule: RoutineSchedule, day: Day): Boolean {
  return when (day) {
    Day.SUNDAY -> schedule.sunday
    Day.MONDAY -> schedule.monday
    Day.TUESDAY -> schedule.tuesday
    Day.WEDNESDAY -> schedule.wednesday
    Day.THURSDAY -> schedule.thursday
    Day.FRIDAY -> schedule.friday
    Day.SATURDAY -> schedule.saturday
  }
}

private fun setScheduleDaySelection(
  schedule: RoutineSchedule, day: Day, value: Boolean
): RoutineSchedule {
  return when (day) {
    Day.SUNDAY -> schedule.copy(sunday = value)
    Day.MONDAY -> schedule.copy(monday = value)
    Day.TUESDAY -> schedule.copy(tuesday = value)
    Day.WEDNESDAY -> schedule.copy(wednesday = value)
    Day.THURSDAY -> schedule.copy(thursday = value)
    Day.FRIDAY -> schedule.copy(friday = value)
    Day.SATURDAY -> schedule.copy(saturday = value)
  }
}