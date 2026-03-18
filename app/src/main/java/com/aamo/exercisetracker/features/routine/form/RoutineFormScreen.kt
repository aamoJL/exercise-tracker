package com.aamo.exercisetracker.features.routine.form

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.aamo.exercisetracker.features.routine.form.components.ScheduleInput
import com.aamo.exercisetracker.features.routine.form.models.RoutineFormFields
import com.aamo.exercisetracker.features.routine.form.use_cases.deleteRoutine
import com.aamo.exercisetracker.features.routine.form.use_cases.fetchRoutineWithSchedule
import com.aamo.exercisetracker.features.routine.form.use_cases.saveRoutine
import com.aamo.exercisetracker.ui.components.HorizontalDividerLabel
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.components.inputs.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.inputs.LoadingIconButton
import com.aamo.exercisetracker.ui.components.inputs.text_field.borderlessTextFieldColors
import com.aamo.exercisetracker.ui.components.modals.DeleteDialog
import com.aamo.exercisetracker.ui.components.modals.UnsavedDialog
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.viewmodels.SavingState
import com.aamo.exercisetracker.utility.viewmodels.ViewModelState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class RoutineFormScreen(val id: Long)

class RoutineFormViewModel(
  private val fetchData: suspend () -> RoutineWithSchedule?,
  private val saveData: suspend (RoutineWithSchedule) -> Unit,
  private val deleteData: suspend (Routine) -> Unit
) : ViewModel() {
  class FormState(fields: RoutineFormFields) {
    val routineName = ViewModelState(fields.name).onChange { onUnsavedChanges() }
    val selectedDays = ViewModelState(fields.days).onChange { onUnsavedChanges() }
    val isNew = fields.name.isEmpty()
    var savingState by mutableStateOf(SavingState())

    fun canSave(): Boolean {
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

  private var model: RoutineWithSchedule? = null

  private val _formState = MutableSharedFlow<FormState?>().onStart {
    val data = fetchData() ?: throw Exception("Failed to fetch data")
    model = data

    emit(
      FormState(
        fields = RoutineFormFields(
          name = data.routine.name, days = data.schedule?.asListOfDays() ?: emptyList()
        )
      )
    )
  }.catch { }
  val formState = _formState.stateIn(
    scope = viewModelScope, started = SharingStarted.Lazily, initialValue = null
  )

  /**
   * Saves the routine to the database
   */
  fun save() {
    val formState = formState.value ?: return
    if (!formState.canSave()) return

    formState.apply { savingState = savingState.getAsSaving() }

    viewModelScope.launch {
      runCatching {
        saveData(formState.let { s ->
          checkNotNull(model).let { model ->
            model.copy(
              routine = model.routine.copy(name = s.routineName.value),
              schedule = (model.schedule ?: RoutineSchedule(routineId = model.routine.id)).copy(
                sunday = s.selectedDays.value.contains(Day.SUNDAY),
                monday = s.selectedDays.value.contains(Day.MONDAY),
                tuesday = s.selectedDays.value.contains(Day.TUESDAY),
                wednesday = s.selectedDays.value.contains(Day.WEDNESDAY),
                thursday = s.selectedDays.value.contains(Day.THURSDAY),
                friday = s.selectedDays.value.contains(Day.FRIDAY),
                saturday = s.selectedDays.value.contains(Day.SATURDAY),
              )
            )
          }
        })
      }.onSuccess { _ ->
        formState.apply { savingState = savingState.getAsSaved() }
      }.onFailure { error ->
        formState.apply { savingState = savingState.getAsError(error = Error(error)) }
      }
    }
  }

  /**
   * Deletes the routine from the database
   */
  fun delete() {
    val state = formState.value ?: return
    if (state.isNew) return

    viewModelScope.launch {
      runCatching { deleteData(checkNotNull(model?.routine)) }
    }
  }
}

fun NavGraphBuilder.routineFormScreen(
  onBack: () -> Unit, onSaved: (id: Long) -> Unit, onDeleted: () -> Unit
) {
  composable<RoutineFormScreen> { navStack ->
    val (routineId) = navStack.toRoute<RoutineFormScreen>()
    val dao = RoutineDatabase.getDatabase(LocalContext.current.applicationContext).routineDao()

    val viewmodel: RoutineFormViewModel = viewModel(factory = viewModelFactory {
      initializer {
        RoutineFormViewModel(
          fetchData = { fetchRoutineWithSchedule(dao = dao, routineId = routineId) },
          saveData = { (routine, schedule) ->
            saveRoutine(dao = dao, routine = routine, schedule = schedule).also { id ->
              onSaved(id)
            }
          },
          deleteData = { routine ->
            deleteRoutine(dao = dao, routine).also {
              onDeleted()
            }
          },
        )
      }
    })
    val formState by viewmodel.formState.collectAsStateWithLifecycle()

    LoadingScreen(model = formState) {
      RoutineFormScreenContent(
        formState = it,
        onBack = onBack,
        onSave = { viewmodel.save() },
        onDelete = { viewmodel.delete() })
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutineFormScreenContent(
  formState: RoutineFormViewModel.FormState,
  onBack: () -> Unit,
  onSave: () -> Unit,
  onDelete: () -> Unit,
) {
  val unsavedChanges =
    remember(formState.savingState.unsavedChanges) { formState.savingState.unsavedChanges }

  var openDeleteDialog by remember { mutableStateOf(false) }
  var openUnsavedDialog by remember { mutableStateOf(false) }

  UnsavedDialog(
    open = openUnsavedDialog,
    onDismiss = { openUnsavedDialog = false },
    onConfirm = {
      openUnsavedDialog = false
      onBack()
    },
  )
  DeleteDialog(
    open = openDeleteDialog,
    title = stringResource(R.string.dialog_title_delete_routine),
    onDismiss = { openDeleteDialog = false },
    onConfirm = {
      openDeleteDialog = false
      onDelete()
    })

  BackHandler(enabled = unsavedChanges) {
    openUnsavedDialog = true
  }

  Scaffold(
    topBar = {
      TopAppBar(title = {
        Text(
          text = ifElse(
            condition = formState.isNew,
            ifTrue = { stringResource(R.string.title_new_routine) },
            ifFalse = { stringResource(R.string.title_existing_routine) })
        )
      }, navigationIcon = {
        BackNavigationIconButton(onBack = {
          if (unsavedChanges) openUnsavedDialog = true else onBack()
        })
      }, actions = {
        if (!formState.isNew) {
          IconButton(onClick = { openDeleteDialog = true }) {
            Icon(
              painter = painterResource(R.drawable.rounded_delete_24),
              contentDescription = stringResource(R.string.cd_delete_routine)
            )
          }
        }
        LoadingIconButton(
          onClick = onSave,
          isLoading = formState.savingState.state == SavingState.State.SAVING,
          enabled = formState.canSave()
        ) {
          Icon(
            painter = painterResource(R.drawable.round_done_24),
            contentDescription = stringResource(R.string.cd_save_routine)
          )
        }
      })
    }, modifier = Modifier.imePadding()
  ) { innerPadding ->
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier
        .padding(innerPadding)
        .padding(8.dp)
    ) {
      Column {
        HorizontalDividerLabel(
          label = stringResource(R.string.label_routine), modifier = Modifier.padding(12.dp)
        )
        ElevatedCard(shape = MaterialTheme.shapes.small) {
          Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
              .padding(8.dp)
              .padding(bottom = 4.dp)
              .fillMaxWidth()
          ) {
            TextField(
              value = formState.routineName.value,
              label = { Text(stringResource(R.string.label_name)) },
              shape = RectangleShape,
              colors = borderlessTextFieldColors(),
              onValueChange = { formState.routineName.update(it) },
              keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Sentences
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalDividerLabel(
          label = stringResource(R.string.label_schedule), modifier = Modifier.padding(12.dp)
        )
        ElevatedCard(shape = RoundedCornerShape(50), modifier = Modifier) {
          ScheduleInput(
            selections = formState.selectedDays.value,
            onChange = { formState.selectedDays.update(it) },
            modifier = Modifier.padding(4.dp)
          )
        }
      }
    }
  }
}

@Suppress("HardCodedStringLiteral")
@Preview
@Composable
private fun Preview() {
  ExerciseTrackerTheme {
    RoutineFormScreenContent(
      formState = RoutineFormViewModel.FormState(
      fields = RoutineFormFields(
        name = "Name", days = listOf(Day.MONDAY, Day.WEDNESDAY, Day.SATURDAY)
      )
    ), onBack = {}, onSave = {}, onDelete = {})
  }
}