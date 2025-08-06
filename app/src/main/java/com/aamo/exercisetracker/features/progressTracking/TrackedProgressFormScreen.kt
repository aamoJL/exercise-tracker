package com.aamo.exercisetracker.features.progressTracking

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.DeleteDialog
import com.aamo.exercisetracker.ui.components.IntNumberField
import com.aamo.exercisetracker.ui.components.LoadingIconButton
import com.aamo.exercisetracker.ui.components.UnsavedDialog
import com.aamo.exercisetracker.ui.components.borderlessTextFieldColors
import com.aamo.exercisetracker.utility.extensions.form.HideZero
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.extensions.general.onTrue
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import com.aamo.exercisetracker.utility.viewmodels.SavingState
import com.aamo.exercisetracker.utility.viewmodels.ViewModelState
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

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
    val isNew: Boolean,
  )

  class UiState {
    val progressName = ViewModelState(String.EMPTY).onChange { onUnsavedChanges() }
    val weeklyInterval = ViewModelState(0).onChange { onUnsavedChanges() }
    val progressValueUnit = ViewModelState(String.EMPTY).onChange { onUnsavedChanges() }
    var savingState by mutableStateOf(SavingState(canSave = { canSave() }))
    var isNew by mutableStateOf(false)

    private fun onUnsavedChanges() {
      if (!savingState.unsavedChanges) {
        savingState = savingState.copy(unsavedChanges = true)
      }
    }

    private fun canSave(): Boolean {
      return when {
        savingState.state == SavingState.State.SAVING -> false
        progressName.value.isEmpty() -> false
        progressValueUnit.value.isEmpty() -> false
        else -> true
      }
    }
  }

  val uiState = UiState()

  init {
    viewModelScope.launch {
      fetchData().also { result ->
        uiState.apply {
          progressName.update(result.trackedProgressName)
          weeklyInterval.update(result.weeklyInterval)
          progressValueUnit.update(result.progressValueUnit)
          isNew = result.isNew
          savingState = savingState.copy(unsavedChanges = false)
        }
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
          ifElse(condition = progressId == 0L, ifTrue = {
            TrackedProgressFormScreenViewModel.Model(
              trackedProgressName = String.EMPTY,
              weeklyInterval = 0,
              progressValueUnit = progressUnitDefault,
              isNew = true
            )
          }, ifFalse = {
            (dao.getTrackedProgress(progressId)
              ?: throw Exception("Failed to fetch data")).let { progress ->
              TrackedProgressFormScreenViewModel.Model(
                trackedProgressName = progress.name,
                weeklyInterval = progress.intervalWeeks,
                progressValueUnit = progress.unit,
                isNew = false
              )
            }
          })
        }, saveData = { model ->
          dao.upsert(
            trackedProgress = TrackedProgress(
              id = progressId,
              name = model.trackedProgressName,
              intervalWeeks = model.weeklyInterval,
              unit = model.progressValueUnit
            )
          ).also { result ->
            ifElse(
              condition = result == -1L,
              ifTrue = { progressId },
              ifFalse = { result }).also { onSaved(it) }
          }.let { true }
        }, deleteData = {
          (dao.getTrackedProgress(progressId)
            ?: throw Exception("Failed to fetch data")).let { result ->
            (dao.delete(result) > 0).onTrue {
              onDeleted()
            }
          }
        })
      }
    })
    val uiState = viewmodel.uiState

    TrackedProgressFormScreen(
      uiState = uiState,
      onBack = onBack,
      onSave = { viewmodel.save() },
      onDelete = { viewmodel.delete() })
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
            imageVector = Icons.Filled.Delete,
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
          imageVector = Icons.Filled.Done,
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
      TextField(
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
    }
  }
}