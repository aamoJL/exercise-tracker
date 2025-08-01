package com.aamo.exercisetracker.features.routine

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.aamo.exercisetracker.R
import com.aamo.exercisetracker.database.RoutineDatabase
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.DeleteDialog
import com.aamo.exercisetracker.ui.components.LoadingIconButton
import com.aamo.exercisetracker.ui.components.UnsavedDialog
import com.aamo.exercisetracker.ui.components.borderlessTextFieldColors
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOrder
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.extensions.general.onFalse
import com.aamo.exercisetracker.utility.extensions.general.onTrue
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import com.aamo.exercisetracker.utility.viewmodels.SavingState
import com.aamo.exercisetracker.utility.viewmodels.ViewModelState
import com.aamo.exercisetracker.utility.viewmodels.ViewModelStateList
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Calendar

@Serializable
data class RoutineFormScreen(val id: Long)

class RoutineFormViewModel(
  private val fetchData: suspend () -> Model,
  private val saveData: suspend (Model) -> Boolean,
  private val deleteData: suspend () -> Boolean
) : ViewModel() {
  data class Model(
    val routineName: String,
    val selectedDays: List<Day>,
    val isNew: Boolean,
  )

  class UiState {
    val routineName = ViewModelState(String.EMPTY).onChange { onUnsavedChanges() }
    val selectedDays = ViewModelStateList<Day>().unique().onChange { onUnsavedChanges() }
    var isNew by mutableStateOf(true)
    var savingState by mutableStateOf(SavingState(canSave = { canSave() }))

    private fun canSave(): Boolean {
      return when {
        savingState.state == SavingState.State.SAVING -> false
        routineName.value.isEmpty() -> false
        else -> true
      }
    }

    private fun onUnsavedChanges() {
      if (!savingState.unsavedChanges) {
        savingState = savingState.copy(unsavedChanges = true)
      }
    }
  }

  val uiState = UiState()

  init {
    viewModelScope.launch {
      fetchData().also { result ->
        uiState.apply {
          routineName.update(result.routineName)
          selectedDays.add(*result.selectedDays.toTypedArray())
          isNew = result.isNew
          savingState = savingState.copy(unsavedChanges = false)
        }
      }
    }
  }

  /**
   * Saves the routine to the database
   */
  fun save() {
    if (!uiState.savingState.canSave()) return

    uiState.apply { savingState = savingState.getAsSaving() }

    viewModelScope.launch {
      runCatching {
        check(saveData(uiState.let { s ->
          Model(
            routineName = s.routineName.value, selectedDays = s.selectedDays.values, isNew = s.isNew
          )
        }))
      }.onSuccess { _ ->
        uiState.apply { savingState = savingState.getAsSaved() }
      }.onFailure { error ->
        uiState.apply { savingState = savingState.getAsError(error = Error(error)) }
      }
    }
  }

  /**
   * Deletes the routine from the database
   */
  fun delete() {
    if (uiState.isNew) return

    viewModelScope.launch {
      runCatching { deleteData() }
    }
  }
}

fun NavGraphBuilder.routineFormScreen(
  onBack: () -> Unit, onSaved: (id: Long) -> Unit, onDeleted: () -> Unit
) {
  composable<RoutineFormScreen> { navStack ->
    var (routineId) = navStack.toRoute<RoutineFormScreen>()
    val dao = RoutineDatabase.getDatabase(LocalContext.current.applicationContext).routineDao()

    val viewmodel: RoutineFormViewModel = viewModel(factory = viewModelFactory {
      initializer {
        RoutineFormViewModel(
          fetchData = {
            ifElse(condition = routineId == 0L, ifTrue = {
              RoutineFormViewModel.Model(
                routineName = String.EMPTY, selectedDays = emptyList(), isNew = true
              )
            }, ifFalse = {
              (dao.getRoutineWithSchedule(routineId)
                ?: throw Exception("Failed to fetch data")).let { result ->
                RoutineFormViewModel.Model(
                  routineName = result.routine.name,
                  selectedDays = result.schedule?.asListOfDays() ?: emptyList(),
                  isNew = false
                )
              }
            })
          },
          saveData = { model ->
            ifElse(condition = model.isNew, ifTrue = {
              RoutineWithSchedule(
                routine = Routine(name = String.EMPTY), schedule = null
              )
            }, ifFalse = {
              dao.getRoutineWithSchedule(routineId) ?: throw Exception("Failed to fetch data")
            }).let { result ->
              result.copy(
                routine = result.routine.copy(name = model.routineName),
                schedule = result.schedule.let { schedule ->
                  (schedule?.copy() ?: RoutineSchedule(routineId = result.routine.id)).copy(
                    sunday = model.selectedDays.contains(Day.SUNDAY),
                    monday = model.selectedDays.contains(Day.MONDAY),
                    tuesday = model.selectedDays.contains(Day.TUESDAY),
                    wednesday = model.selectedDays.contains(Day.WEDNESDAY),
                    thursday = model.selectedDays.contains(Day.THURSDAY),
                    friday = model.selectedDays.contains(Day.FRIDAY),
                    saturday = model.selectedDays.contains(Day.SATURDAY),
                  )
                })
            }.let { routine ->
              dao.upsertAndGet(routine).let { result ->
                (result != null).onTrue {
                  result?.also {
                    routineId = result.routine.id
                    onSaved(routineId)
                  }
                }
              }
            }
          },
          deleteData = {
            if (routineId == 0L) false

            (dao.getRoutine(routineId) ?: throw Exception("Failed to fetch data")).let { result ->
              (dao.delete(result) > 0).onTrue {
                onDeleted()
              }
            }
          },
        )
      }
    })

    RoutineFormScreen(
      uiState = viewmodel.uiState,
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
  val days = remember { Calendar.getInstance().getLocalDayOrder() }
  val unsavedChanges =
    remember(uiState.savingState.unsavedChanges) { uiState.savingState.unsavedChanges }

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
    DeleteDialog(
      title = stringResource(R.string.dialog_title_delete_routine),
      onDismiss = { openDeleteDialog = false },
      onConfirm = {
        openDeleteDialog = false
        onDelete()
      })
  }

  BackHandler(enabled = unsavedChanges) {
    openUnsavedDialog = true
  }

  Scaffold(topBar = {
    TopAppBar(title = {
      Text(
        text = ifElse(
          condition = uiState.isNew,
          ifTrue = { stringResource(R.string.title_new_routine) },
          ifFalse = { stringResource(R.string.title_edit_routine) })
      )
    }, navigationIcon = {
      BackNavigationIconButton(onBack = {
        if (unsavedChanges) openUnsavedDialog = true else onBack()
      })
    }, actions = {
      if (!uiState.isNew) {
        IconButton(onClick = { openDeleteDialog = true }) {
          Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = stringResource(R.string.cd_delete_routine)
          )
        }
      }
      LoadingIconButton(
        onClick = onSave,
        isLoading = uiState.savingState.state == SavingState.State.SAVING,
        enabled = uiState.savingState.canSave()
      ) {
        Icon(
          imageVector = Icons.Filled.Done,
          contentDescription = stringResource(R.string.cd_save_routine)
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
        value = uiState.routineName.value,
        label = { Text(stringResource(R.string.label_name)) },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        onValueChange = { uiState.routineName.update(it) },
        keyboardOptions = KeyboardOptions(
          imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Sentences
        ),
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      Card {
        Column(modifier = Modifier.padding(8.dp)) {
          Text(
            text = stringResource(R.string.label_schedule),
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
              val isSelected = uiState.selectedDays.values.contains(day)

              IconToggleButton(
                colors = IconButtonDefaults.outlinedIconToggleButtonColors(
                  checkedContainerColor = MaterialTheme.colorScheme.inversePrimary,
                  checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                  contentColor = MaterialTheme.colorScheme.outline,
                ), checked = isSelected, onCheckedChange = { selection ->
                  selection.onTrue {
                    uiState.selectedDays.add(day)
                  }.onFalse {
                    uiState.selectedDays.remove(day)
                  }
                }, modifier = Modifier
              ) {
                Text(stringResource(day.nameResourceKey).take(2))
              }
            }
          }
        }
      }
    }
  }
}