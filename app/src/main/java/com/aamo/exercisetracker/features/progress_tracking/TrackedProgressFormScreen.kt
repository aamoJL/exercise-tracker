package com.aamo.exercisetracker.features.progress_tracking

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
import com.aamo.exercisetracker.features.progress_tracking.use_cases.deleteTrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.use_cases.fromDao
import com.aamo.exercisetracker.features.progress_tracking.use_cases.saveTrackedProgress
import com.aamo.exercisetracker.features.progress_tracking.use_cases.toDao
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.DeleteDialog
import com.aamo.exercisetracker.ui.components.DurationNumberField
import com.aamo.exercisetracker.ui.components.DurationNumberFieldFields
import com.aamo.exercisetracker.ui.components.IntNumberField
import com.aamo.exercisetracker.ui.components.LoadingIconButton
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.components.UnsavedDialog
import com.aamo.exercisetracker.ui.components.borderlessTextFieldColors
import com.aamo.exercisetracker.utility.extensions.form.HideZero
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import com.aamo.exercisetracker.utility.extensions.general.equalsAny
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.extensions.general.letIf
import com.aamo.exercisetracker.utility.extensions.general.onTrue
import com.aamo.exercisetracker.utility.viewmodels.SavingState
import com.aamo.exercisetracker.utility.viewmodels.ViewModelState
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Serializable
data class TrackedProgressFormScreen(val progressId: Long)

class TrackedProgressFormScreenViewModel(
  private val fetchData: suspend () -> Model,
  private val saveData: suspend (Model) -> Boolean,
  private val deleteData: suspend () -> Boolean,
) : ViewModel() {
  data class Model(
    val trackedProgressName: String,
    val weeklyInterval: Int,
    val progressValueUnit: String,
    val hasStopWatch: Boolean,
    val timerDuration: Duration?,
    val isNew: Boolean,
  ) {
    companion object
  }

  class UiState {
    enum class ProgressType {
      REPETITION,
      TIMER,
      STOPWATCH
    }

    val progressName = ViewModelState(String.EMPTY).onChange { onUnsavedChanges() }
    val weeklyInterval = ViewModelState(0).onChange { onUnsavedChanges() }
    val progressValueUnit = ViewModelState(String.EMPTY).onChange { onUnsavedChanges() }
    val progressType = ViewModelState(ProgressType.REPETITION).onChange {
      if (it != ProgressType.TIMER) timerDuration.update(0.seconds)
    }
    val timerDuration = ViewModelState(0.seconds).onChange { onUnsavedChanges() }
    var savingState by mutableStateOf(SavingState(canSave = { canSave() }))
    var isNew by mutableStateOf(false)

    private fun onUnsavedChanges() {
      if (!savingState.unsavedChanges) {
        savingState = savingState.copy(unsavedChanges = true)
      }
    }

    private fun canSave(): Boolean {
      if (progressType.value == ProgressType.TIMER && timerDuration.value < 1.seconds) return false
      if (savingState.state == SavingState.State.SAVING) return false
      if (progressName.value.isEmpty()) return false
      return true
    }
  }

  var isLoading by mutableStateOf(true)
  val uiState = UiState()

  init {
    viewModelScope.launch {
      fetchData().also { result ->
        uiState.apply {
          progressName.update(result.trackedProgressName)
          weeklyInterval.update(result.weeklyInterval)
          progressValueUnit.update(result.progressValueUnit)
          result.hasStopWatch.onTrue {
            progressType.update(UiState.ProgressType.STOPWATCH)
          }
          result.timerDuration?.also {
            progressType.update(UiState.ProgressType.TIMER)
            timerDuration.update(result.timerDuration)
          }
          isNew = result.isNew
          savingState = savingState.copy(unsavedChanges = false)
        }
        isLoading = false
      }
    }
  }

  fun save() {
    if (!uiState.savingState.canSave()) return

    uiState.apply { savingState = savingState.getAsSaving() }

    viewModelScope.launch {
      runCatching {
        check(saveData(uiState.let { s ->
          Model(
            trackedProgressName = s.progressName.value,
            weeklyInterval = s.weeklyInterval.value,
            progressValueUnit = s.progressValueUnit.value,
            hasStopWatch = s.progressType.value == UiState.ProgressType.STOPWATCH,
            timerDuration = s.timerDuration.value.let { if (it.inWholeSeconds == 0L) null else it },
            isNew = s.isNew
          )
        }))
      }.onSuccess { _ ->
        uiState.apply { savingState = savingState.getAsSaved() }
      }.onFailure { error ->
        uiState.apply { savingState = savingState.getAsError(error = Error(error)) }
      }
    }
  }

  fun delete() {
    if (uiState.isNew) return

    viewModelScope.launch {
      runCatching { deleteData() }
    }
  }
}

fun NavGraphBuilder.trackedProgressFormScreen(
  onBack: () -> Unit, onSaved: (id: Long) -> Unit, onDeleted: () -> Unit
) {
  composable<TrackedProgressFormScreen> { navStack ->
    val progressId = navStack.toRoute<TrackedProgressFormScreen>().progressId
    val progressUnitDefault = stringResource(R.string.ph_reps)
    val dao =
      RoutineDatabase.getDatabase(LocalContext.current.applicationContext).trackedProgressDao()
    val viewmodel: TrackedProgressFormScreenViewModel = viewModel(factory = viewModelFactory {
      initializer {
        TrackedProgressFormScreenViewModel(fetchData = {
          TrackedProgressFormScreenViewModel.Model.fromDao(defaultUnit = progressUnitDefault) {
            dao.getTrackedProgress(progressId) ?: TrackedProgress()
          }
        }, saveData = { model ->
          saveTrackedProgress(data = model.toDao(progressId)) {
            dao.upsert(it).let { result ->
              ifElse(
                condition = result != -1L,
                ifTrue = { onSaved(result) },
                ifFalse = { onSaved(it.id) })
            }.let { true }
          }
        }, deleteData = {
          dao.getTrackedProgress(progressId)?.let { progress ->
            deleteTrackedProgress(progress) { dao.delete(*it.toTypedArray()) > 0 }.onTrue { onDeleted() }
          } ?: throw Exception("Failed to fetch data")
        })
      }
    })
    val uiState = viewmodel.uiState

    LoadingScreen(enabled = viewmodel.isLoading) {
      TrackedProgressFormScreen(
        uiState = uiState,
        onBack = onBack,
        onSave = { viewmodel.save() },
        onDelete = { viewmodel.delete() })
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackedProgressFormScreen(
  uiState: TrackedProgressFormScreenViewModel.UiState,
  onBack: () -> Unit,
  onSave: () -> Unit,
  onDelete: () -> Unit,
) {
  val progressUnitDefault = stringResource(R.string.ph_reps)

  val unitFieldEnabled by remember(uiState.progressType.value) {
    mutableStateOf(
      uiState.progressType.value.equalsAny(
        TrackedProgressFormScreenViewModel.UiState.ProgressType.REPETITION,
        TrackedProgressFormScreenViewModel.UiState.ProgressType.TIMER
      )
    )
  }
  val durationFieldEnabled by remember(uiState.progressType.value) {
    mutableStateOf(
      uiState.progressType.value.equalsAny(
        TrackedProgressFormScreenViewModel.UiState.ProgressType.TIMER,
      )
    )
  }
  var unitFieldPreviousValue by remember {
    mutableStateOf(
      uiState.progressValueUnit.value.letIf(
        condition = { it.isEmpty() },
        block = { progressUnitDefault })
    )
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
      title = stringResource(R.string.dialog_title_delete_tracked_progress),
      onDismiss = { openDeleteDialog = false },
      onConfirm = {
        openDeleteDialog = false
        onDelete()
      })
  }

  BackHandler(enabled = uiState.savingState.unsavedChanges) {
    openUnsavedDialog = true
  }

  Scaffold(topBar = {
    TopAppBar(title = {
      Text(
        text = ifElse(
          condition = uiState.isNew,
          ifTrue = { stringResource(R.string.title_new_tracked_progress) },
          ifFalse = { stringResource(R.string.title_edit_tracked_progress) })
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
        enabled = uiState.savingState.canSave()
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
      IntNumberField(
        value = uiState.weeklyInterval.value,
        label = { Text(stringResource(R.string.label_weekly_interval_optional)) },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        onValueChange = { uiState.weeklyInterval.update(it) },
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
            title = stringResource(R.string.title_repetitions),
            selected = uiState.progressType.value == TrackedProgressFormScreenViewModel.UiState.ProgressType.REPETITION,
            onSelect = { uiState.progressType.update(TrackedProgressFormScreenViewModel.UiState.ProgressType.REPETITION) },
          )
          HorizontalRadioButton(
            title = stringResource(R.string.label_timer),
            selected = uiState.progressType.value == TrackedProgressFormScreenViewModel.UiState.ProgressType.TIMER,
            onSelect = { uiState.progressType.update(TrackedProgressFormScreenViewModel.UiState.ProgressType.TIMER) },
          )
          HorizontalRadioButton(
            title = stringResource(R.string.label_stopwatch),
            selected = uiState.progressType.value == TrackedProgressFormScreenViewModel.UiState.ProgressType.STOPWATCH,
            onSelect = { uiState.progressType.update(TrackedProgressFormScreenViewModel.UiState.ProgressType.STOPWATCH) },
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