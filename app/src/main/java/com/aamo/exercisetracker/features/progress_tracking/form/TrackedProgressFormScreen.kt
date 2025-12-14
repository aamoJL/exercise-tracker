package com.aamo.exercisetracker.features.progress_tracking.form

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
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
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.form.models.TrackedProgressFormFields
import com.aamo.exercisetracker.features.progress_tracking.form.use_cases.deleteTrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.form.use_cases.fetchTrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.form.use_cases.saveTrackedProgress
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.components.inputs.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.inputs.LoadingIconButton
import com.aamo.exercisetracker.ui.components.inputs.number_field.DurationNumberField
import com.aamo.exercisetracker.ui.components.inputs.number_field.DurationNumberFieldFields
import com.aamo.exercisetracker.ui.components.inputs.number_field.IntFieldValidator
import com.aamo.exercisetracker.ui.components.inputs.number_field.NumberField
import com.aamo.exercisetracker.ui.components.inputs.text_field.borderlessTextFieldColors
import com.aamo.exercisetracker.ui.components.modals.DeleteDialog
import com.aamo.exercisetracker.ui.components.modals.UnsavedDialog
import com.aamo.exercisetracker.utility.extensions.form.HideZero
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import com.aamo.exercisetracker.utility.extensions.general.equalsAny
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.extensions.general.letIf
import com.aamo.exercisetracker.utility.viewmodels.SavingState
import com.aamo.exercisetracker.utility.viewmodels.ViewModelState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Serializable
data class TrackedProgressFormScreen(val progressId: Long)

class TrackedProgressFormViewModel(
  private val fetchData: suspend () -> TrackedProgress?,
  private val saveData: suspend (TrackedProgress) -> Unit,
  private val deleteData: suspend (TrackedProgress) -> Unit,
) : ViewModel() {
  class FormState(fields: TrackedProgressFormFields) {
    val progressName = ViewModelState(fields.name).onChange { onUnsavedChanges() }
    val weeklyInterval = ViewModelState(fields.weeklyInterval).onChange { onUnsavedChanges() }
    val progressValueUnit = ViewModelState(fields.progressValueUnit).onChange { onUnsavedChanges() }
    val progressType = ViewModelState(fields.type).onChange {
      if (it != TrackedProgressFormFields.ProgressType.TIMER) timerDuration.update(0.seconds)
    }
    val timerDuration =
      ViewModelState(fields.timerDuration ?: 0.seconds).onChange { onUnsavedChanges() }
    var savingState by mutableStateOf(SavingState())
    var isNew by mutableStateOf(fields.name.isEmpty())

    private fun onUnsavedChanges() {
      if (!savingState.unsavedChanges) {
        savingState = savingState.copy(unsavedChanges = true)
      }
    }

    fun canSave(): Boolean {
      if (progressName.value.isEmpty()) return false
      if (progressType.value == TrackedProgressFormFields.ProgressType.TIMER) {
        if (timerDuration.value < 1.seconds) return false
      }
      if (savingState.state == SavingState.State.SAVING) return false
      return true
    }
  }

  private var model: TrackedProgress? = null

  private val _formState = MutableSharedFlow<FormState?>().onStart {
    val data = fetchData() ?: throw Exception("Failed to fetch data")
    model = data

    emit(
      FormState(
        fields = TrackedProgressFormFields(
          name = data.name,
          weeklyInterval = data.intervalWeeks,
          type = if (data.hasStopWatch) TrackedProgressFormFields.ProgressType.STOPWATCH
          else if (data.timerTime != null) TrackedProgressFormFields.ProgressType.TIMER
          else TrackedProgressFormFields.ProgressType.REPETITION,
          progressValueUnit = data.unit,
          timerDuration = data.timerTime?.milliseconds
        )
      )
    )
  }.catch { }
  val formState = _formState.stateIn(
    scope = viewModelScope, started = SharingStarted.Lazily, initialValue = null
  )

  fun save() {
    val formState = formState.value ?: return
    if (!formState.canSave()) return

    formState.apply { savingState = savingState.getAsSaving() }

    viewModelScope.launch {
      runCatching {
        saveData(formState.let { s ->
          checkNotNull(model).let { model ->
            model.copy(
              name = s.progressName.value,
              intervalWeeks = s.weeklyInterval.value,
              unit = s.progressValueUnit.value,
              hasStopWatch = s.progressType.value == TrackedProgressFormFields.ProgressType.STOPWATCH,
              timerTime = s.timerDuration.value.inWholeMilliseconds.let {
                if (it == 0L) null else it
              },
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

  fun delete() {
    val formState = formState.value ?: return
    if (formState.isNew) return

    viewModelScope.launch {
      runCatching { deleteData(checkNotNull(model)) }
    }
  }
}

fun NavGraphBuilder.trackedProgressFormScreen(
  onBack: () -> Unit, onSaved: (id: Long) -> Unit, onDeleted: () -> Unit
) {
  composable<TrackedProgressFormScreen> { navStack ->
    val progressId = navStack.toRoute<TrackedProgressFormScreen>().progressId
    val defaultUnit = stringResource(R.string.default_text_reps)
    val dao =
      RoutineDatabase.getDatabase(LocalContext.current.applicationContext).trackedProgressDao()
    val viewmodel: TrackedProgressFormViewModel = viewModel(factory = viewModelFactory {
      initializer {
        TrackedProgressFormViewModel(fetchData = {
          fetchTrackedProgress(dao = dao, progressId = progressId, defaultUnit = defaultUnit)
        }, saveData = { model ->
          saveTrackedProgress(dao = dao, model = model).also {
            onSaved(it)
          }
        }, deleteData = { model ->
          deleteTrackedProgress(dao = dao, model = model).also {
            onDeleted()
          }
        })
      }
    })
    val formState by viewmodel.formState.collectAsStateWithLifecycle()

    LoadingScreen(loading = formState == null) {
      TrackedProgressFormScreen(
        uiState = checkNotNull(formState),
        onBack = onBack,
        onSave = { viewmodel.save() },
        onDelete = { viewmodel.delete() })
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackedProgressFormScreen(
  uiState: TrackedProgressFormViewModel.FormState,
  onBack: () -> Unit,
  onSave: () -> Unit,
  onDelete: () -> Unit,
) {
  val progressUnitDefault = stringResource(R.string.default_text_reps)

  val unitFieldEnabled by remember(uiState.progressType.value) {
    mutableStateOf(
      uiState.progressType.value.equalsAny(
        TrackedProgressFormFields.ProgressType.REPETITION,
        TrackedProgressFormFields.ProgressType.TIMER
      )
    )
  }
  val durationFieldEnabled by remember(uiState.progressType.value) {
    mutableStateOf(
      uiState.progressType.value.equalsAny(
        TrackedProgressFormFields.ProgressType.TIMER,
      )
    )
  }
  var unitFieldPreviousValue by remember {
    mutableStateOf(uiState.progressValueUnit.value.letIf(condition = { it.isEmpty() }) { progressUnitDefault })
  }
  var durationFieldPreviousValue by remember { mutableStateOf(uiState.timerDuration.value) }

  var openDeleteDialog by remember { mutableStateOf(false) }
  var openUnsavedDialog by remember { mutableStateOf(false) }

  LaunchedEffect(uiState.progressValueUnit.value) {
    if (unitFieldEnabled) {
      unitFieldPreviousValue = uiState.progressValueUnit.value
    }
  }

  LaunchedEffect(uiState.timerDuration.value) {
    if (durationFieldEnabled) {
      durationFieldPreviousValue = uiState.timerDuration.value
    }
  }

  LaunchedEffect(unitFieldEnabled) {
    ifElse(
      condition = unitFieldEnabled,
      ifTrue = { uiState.progressValueUnit.update(unitFieldPreviousValue) },
      ifFalse = { uiState.progressValueUnit.update(String.EMPTY) })
  }

  LaunchedEffect(durationFieldEnabled) {
    ifElse(
      condition = durationFieldEnabled,
      ifTrue = { uiState.timerDuration.update(durationFieldPreviousValue) },
      ifFalse = { uiState.timerDuration.update(0.seconds) })
  }

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
    title = stringResource(R.string.dialog_title_delete_tracked_progress),
    onDismiss = { openDeleteDialog = false },
    onConfirm = {
      openDeleteDialog = false
      onDelete()
    })

  BackHandler(enabled = uiState.savingState.unsavedChanges) {
    openUnsavedDialog = true
  }

  Scaffold(topBar = {
    TopAppBar(title = {
      Text(
        text = ifElse(
          condition = uiState.isNew,
          ifTrue = { stringResource(R.string.title_new_tracked_progress) },
          ifFalse = { stringResource(R.string.title_existing_tracked_progress) })
      )
    }, navigationIcon = {
      BackNavigationIconButton(onBack = {
        if (uiState.savingState.unsavedChanges) openUnsavedDialog = true else onBack()
      })
    }, actions = {
      if (!uiState.isNew) {
        IconButton(onClick = { openDeleteDialog = true }) {
          Icon(
            painter = painterResource(R.drawable.rounded_delete_24),
            contentDescription = stringResource(R.string.cd_delete_tracked_progress)
          )
        }
      }
      LoadingIconButton(
        onClick = onSave,
        isLoading = uiState.savingState.state == SavingState.State.SAVING,
        enabled = uiState.canSave()
      ) {
        Icon(
          painter = painterResource(R.drawable.round_done_24),
          contentDescription = stringResource(R.string.cd_save_tracked_progress)
        )
      }
    })
  }) { innerPadding ->
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .padding(innerPadding)
        .padding(8.dp)
    ) {
      TextField(
        value = uiState.progressName.value,
        label = { Text(stringResource(R.string.label_name)) },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        onValueChange = { uiState.progressName.update(it) },
        keyboardOptions = KeyboardOptions(
          imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Sentences
        ),
        modifier = Modifier.fillMaxWidth()
      )
      NumberField(
        value = uiState.weeklyInterval.value,
        onValueChange = { uiState.weeklyInterval.update(it) },
        validator = IntFieldValidator,
        label = { Text(stringResource(R.string.label_weekly_interval_optional)) },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        suffix = { Text(stringResource(R.string.suffix_weeks)) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        visualTransformation = VisualTransformation.HideZero,
        modifier = Modifier.fillMaxWidth()
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(intrinsicSize = IntrinsicSize.Max)
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .selectableGroup()
            .weight(1f)
            .fillMaxHeight()
        ) {
          HorizontalRadioButton(
            title = stringResource(R.string.label_repetitions),
            selected = uiState.progressType.value == TrackedProgressFormFields.ProgressType.REPETITION,
            onSelect = { uiState.progressType.update(TrackedProgressFormFields.ProgressType.REPETITION) },
          )
          HorizontalRadioButton(
            title = stringResource(R.string.label_timer),
            selected = uiState.progressType.value == TrackedProgressFormFields.ProgressType.TIMER,
            onSelect = { uiState.progressType.update(TrackedProgressFormFields.ProgressType.TIMER) },
          )
          HorizontalRadioButton(
            title = stringResource(R.string.label_stopwatch),
            selected = uiState.progressType.value == TrackedProgressFormFields.ProgressType.STOPWATCH,
            onSelect = { uiState.progressType.update(TrackedProgressFormFields.ProgressType.STOPWATCH) },
          )
        }
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
        ) {
          TextField(
            enabled = unitFieldEnabled,
            value = uiState.progressValueUnit.value,
            label = { Text(stringResource(R.string.label_progress_unit)) },
            shape = RectangleShape,
            colors = borderlessTextFieldColors(),
            onValueChange = { uiState.progressValueUnit.update(it) },
            keyboardOptions = KeyboardOptions(
              imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.None
            ),
            modifier = Modifier.fillMaxWidth()
          )
          DurationNumberField(
            enabled = durationFieldEnabled,
            fields = DurationNumberFieldFields(hours = DurationNumberFieldFields.Properties(enabled = false)),
            value = uiState.timerDuration.value,
            onValueChange = { uiState.timerDuration.update(it) },
            shape = RectangleShape,
            colors = borderlessTextFieldColors(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }
  }
}

@Composable
private fun HorizontalRadioButton(
  title: String, selected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier
      .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
      .height(56.dp)
      .padding(horizontal = 8.dp)
  ) {
    RadioButton(selected = selected, onClick = null)
    Text(text = title)
  }
}