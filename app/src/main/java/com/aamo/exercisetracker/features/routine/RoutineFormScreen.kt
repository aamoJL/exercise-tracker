package com.aamo.exercisetracker.features.routine

import android.content.Context
import android.util.Log
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
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
import com.aamo.exercisetracker.ui.components.borderlessTextFieldColors
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOrder
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
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
    val canSave: Boolean = false
  )

  private var database: RoutineDatabase = RoutineDatabase.getDatabase(context)

  private val _uiState = MutableStateFlow(RoutineFormUiState())
  var uiState = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      database.routineDao().getRoutineWithSchedule(routineId)?.let { rws ->
        update(
          RoutineFormUiState(
            routine = rws.routine,
            schedule = rws.schedule ?: RoutineSchedule(routineId = rws.routine.id)
          ).let { it -> it.copy(canSave = canSave(it)) })
      }
    }
  }

  fun update(uiState: RoutineFormUiState) {
    _uiState.update { uiState.copy(canSave = canSave(uiState)) }
  }

  fun canSave(uiState: RoutineFormUiState): Boolean {
    return uiState.routine.name.isNotEmpty()
  }

  /**
   * Saves the routine to the database
   * @return routine id, if the routine was saved, otherwise null
   */
  suspend fun save(): Long? {
    if (!canSave(_uiState.value)) return null

    return database.routineDao().runCatching {
      upsert(
        RoutineWithSchedule(routine = _uiState.value.routine, schedule = _uiState.value.schedule)
      )
    }.onFailure { fail ->
      Log.d("error", fail.message.toString())
    }.onSuccess { success ->
      Log.d("success", success.first.toString())
    }.getOrNull()?.first
  }
}

fun NavGraphBuilder.routineFormScreen(onBack: () -> Unit, onSuccess: (id: Long) -> Unit) {
  composable<RoutineFormScreen> { navStack ->
    val (id) = navStack.toRoute<RoutineFormScreen>()
    val context = LocalContext.current.applicationContext
    val viewmodel: RoutineFormViewModel = viewModel(factory = viewModelFactory {
      initializer { RoutineFormViewModel(context = context, routineId = id) }
    })
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()

    RoutineFormScreen(
      uiState = uiState,
      onStateChanged = { viewmodel.update(it) },
      onBack = onBack,
      onSave = { routineWithSchedule ->
        viewmodel.viewModelScope.launch {
          viewmodel.save()?.let { id ->
            onSuccess(id)
          }
        }
      })
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineFormScreen(
  uiState: RoutineFormUiState,
  onStateChanged: (RoutineFormUiState) -> Unit,
  onBack: () -> Unit,
  onSave: (RoutineWithSchedule) -> Unit
) {
  val focusManager = LocalFocusManager.current
  val days = Calendar.getInstance().getLocalDayOrder()
  val routine = uiState.routine
  val schedule = uiState.schedule

  Scaffold(topBar = {
    TopAppBar(
      title = { Text(text = if (routine.id == 0L) "New Routine" else "Edit Routine") },
      navigationIcon = { BackNavigationIconButton(onBack = onBack) },
      actions = {
        IconButton(onClick = {
          onSave(RoutineWithSchedule(routine = routine, schedule = schedule))
        }, enabled = uiState.canSave) {
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
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
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