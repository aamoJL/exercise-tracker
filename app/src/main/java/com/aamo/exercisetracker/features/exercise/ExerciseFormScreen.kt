package com.aamo.exercisetracker.features.exercise

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.FormList
import com.aamo.exercisetracker.ui.components.IntNumberField
import com.aamo.exercisetracker.ui.components.LoadingIconButton
import com.aamo.exercisetracker.ui.components.UnsavedDialog
import com.aamo.exercisetracker.ui.components.borderlessTextFieldColors
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.extensions.general.onNotNull
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import com.aamo.exercisetracker.utility.viewmodels.SavingState
import com.aamo.exercisetracker.utility.viewmodels.ViewModelState
import com.aamo.exercisetracker.utility.viewmodels.ViewModelStateList
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Serializable
data class EditExerciseFormScreen(val id: Long)

@Serializable
data class AddExerciseFormScreen(val routineId: Long)

class ExerciseFormViewModel(
  private val fetchData: (suspend () -> ExerciseWithSets?),
  private val saveData: (suspend (ExerciseWithSets) -> ExerciseWithSets?),
  private val deleteData: (suspend (Exercise) -> Unit),
  private val resourceProvider: ResourceProvider,
) : ViewModel() {
  // TODO: how to manage resources in viewmodel?
  data class ResourceProvider(val minutesDefault: String)

  inner class UiState(private val onModelChanged: () -> Unit) {
    val exercise = ViewModelState(Exercise(restDuration = 1.minutes)).onChange { onModelChanged() }
    /** Pair or exercise set and its list key */
    val sets =
      ViewModelStateList<Pair<Int, ViewModelState<ExerciseSet>>>().onChange { onModelChanged() }
    var savingState by mutableStateOf(SavingState())
    var deleted by mutableStateOf(false)
    var setUnit = ViewModelState(String.EMPTY).onChange {
      sets.values.forEach { set -> set.second.update(set.second.value.copy(unit = it)) }
    }
    var hasTimer = ViewModelState(false).onChange {
      if (it) {
        setUnit.update(resourceProvider.minutesDefault)
        sets.values.forEach { set ->
          set.second.update(set.second.value.copy(valueType = ExerciseSet.ValueType.COUNTDOWN))
        }
      }
      else {
        sets.values.forEach { set ->
          set.second.update(set.second.value.copy(valueType = ExerciseSet.ValueType.REPETITION))
        }
      }
    }
    private var nextSetKey = 0

    fun addSet() {
      addSet(ExerciseSet())
    }

    fun addSet(vararg sets: ExerciseSet) {
      val setMap = sets.map { set ->
        Pair(nextSetKey, ViewModelState(set).validation { set ->
          if (set.valueType == ExerciseSet.ValueType.COUNTDOWN && set.value.minutes.inWholeMilliseconds > Int.MAX_VALUE.toLong()) {
            set.copy(value = Int.MAX_VALUE.milliseconds.inWholeMinutes.toInt())
          }
          else set
        }.onChange { onModelChanged() }).also { nextSetKey++ }
      }.toTypedArray()

      this.sets.add(*setMap)
    }
  }

  val uiState = UiState(onModelChanged = { onModelChanged() })

  init {
    viewModelScope.launch {
      fetchData()?.let { ews ->
        uiState.apply {
          exercise.update(ews.exercise)
          addSet(*ews.sets.map { set ->
            if (set.valueType == ExerciseSet.ValueType.COUNTDOWN) {
              // Set value to minutes, instead of milliseconds
              set.copy(value = set.value.milliseconds.inWholeMinutes.toInt())
            }
            else set
          }.toTypedArray())
          ews.sets.firstOrNull().onNotNull {
            setUnit.update(it.unit)
            hasTimer.update(it.valueType == ExerciseSet.ValueType.COUNTDOWN)
          }
        }
      }
      uiState.savingState = SavingState(canSave = { canSave() })
    }
  }

  fun save() {
    if (!canSave()) return

    uiState.apply { savingState = savingState.getAsSaving() }

    viewModelScope.launch {
      runCatching {
        checkNotNull(
          saveData(
            ExerciseWithSets(
              exercise = uiState.exercise.value, sets = ifElse(
                condition = uiState.hasTimer.value,
                onTrue = uiState.sets.values.map { it.second.value.copy(value = it.second.value.value.minutes.inWholeMilliseconds.toInt()) },
                onFalse = uiState.sets.values.map { it.second.value })
            )
          )
        )
      }.onSuccess { result ->
        uiState.apply {
          sets.clear().also {
            addSet(*result.sets.toTypedArray())
          }
          exercise.update(result.exercise)
          savingState = savingState.getAsSaved()
        }
      }.onFailure { error ->
        uiState.apply { savingState = savingState.getAsError(error = Error(error)) }
      }
    }
  }

  /**
   * Deletes the exercise from the database
   */
  fun delete() {
    if (uiState.exercise.value.id == 0L) return

    viewModelScope.launch {
      runCatching {
        deleteData(uiState.exercise.value)
      }.onSuccess {
        uiState.deleted = true
      }
    }
  }

  private fun canSave(): Boolean {
    if (uiState.exercise.value.routineId == 0L) return false
    if (uiState.savingState.state == SavingState.State.SAVING) return false
    if (uiState.sets.values.any { it.second.value.value == 0 }) return false
    if (uiState.exercise.value.name.isEmpty()) return false
    if (uiState.sets.values.isEmpty()) return false
    return true
  }

  private fun onModelChanged() {
    uiState.savingState = uiState.savingState.copy(unsavedChanges = true)
  }
}

fun NavGraphBuilder.addExerciseFormScreen(onBack: () -> Unit, onSaved: (id: Long) -> Unit) {
  composable<AddExerciseFormScreen> { navStack ->
    val (routineId) = navStack.toRoute<AddExerciseFormScreen>()
    val dao = RoutineDatabase.getDatabase(LocalContext.current.applicationContext).routineDao()
    val defaultUnit = stringResource(R.string.ph_reps)

    Screen(
      fetchData = {
      ExerciseWithSets(
        exercise = Exercise(routineId = routineId), sets = listOf(ExerciseSet(unit = defaultUnit))
      )
    },
      saveData = { dao.upsertAndGet(it) },
      deleteData = { dao.delete(it) },
      onBack = onBack,
      onSaved = onSaved
    )
  }
}

fun NavGraphBuilder.editExerciseFormScreen(
  onBack: () -> Unit, onSaved: (id: Long) -> Unit, onDeleted: () -> Unit
) {
  composable<EditExerciseFormScreen> { navStack ->
    val (exerciseId) = navStack.toRoute<EditExerciseFormScreen>()
    val dao = RoutineDatabase.getDatabase(LocalContext.current.applicationContext).routineDao()

    Screen(
      fetchData = { dao.getExerciseWithSets(exerciseId) },
      saveData = { dao.upsertAndGet(it) },
      deleteData = { dao.delete(it) },
      onBack = onBack,
      onSaved = onSaved,
      onDeleted = onDeleted,
    )
  }
}

@Composable
private fun Screen(
  fetchData: suspend () -> ExerciseWithSets?,
  saveData: suspend (ExerciseWithSets) -> ExerciseWithSets?,
  deleteData: suspend (Exercise) -> Unit,
  onBack: () -> Unit,
  onSaved: (id: Long) -> Unit,
  onDeleted: () -> Unit = {},
) {
  val resourceProvider = ExerciseFormViewModel.ResourceProvider(
    minutesDefault = stringResource(R.string.ph_minutes),
  )
  val viewmodel: ExerciseFormViewModel = viewModel(factory = viewModelFactory {
    initializer {
      ExerciseFormViewModel(
        fetchData = fetchData,
        saveData = saveData,
        deleteData = deleteData,
        resourceProvider = resourceProvider
      )
    }
  })
  val uiState = viewmodel.uiState

  UiStateEffects(uiState = uiState, onDeleted = onDeleted, onSaved = onSaved)

  ExerciseFormScreen(
    uiState = uiState,
    onBack = onBack,
    onSave = { viewmodel.save() },
    onDelete = { viewmodel.delete() })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseFormScreen(
  uiState: ExerciseFormViewModel.UiState,
  onBack: () -> Unit,
  onSave: () -> Unit,
  onDelete: () -> Unit,
) {
  val exercise = remember(uiState.exercise) { uiState.exercise }
  val sets = remember(uiState.sets) { uiState.sets }
  val unsavedChanges =
    remember(uiState.savingState.unsavedChanges) { uiState.savingState.unsavedChanges }
  val isNew = remember(exercise.value.id) { exercise.value.id == 0L }

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
    TopAppBar(title = {
      Text(
        text = ifElse(
          condition = isNew,
          onTrue = stringResource(R.string.title_new_exercise),
          onFalse = stringResource(R.string.title_edit_exercise)
        )
      )
    }, navigationIcon = {
      BackNavigationIconButton(onBack = {
        if (unsavedChanges) openUnsavedDialog = true else onBack()
      })
    }, actions = {
      if (!isNew) {
        IconButton(onClick = { openDeleteDialog = true }) {
          Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = stringResource(R.string.cd_delete_exercise)
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
          contentDescription = stringResource(R.string.cd_save_exercise)
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
        value = exercise.value.name,
        label = { Text(stringResource(R.string.label_name)) },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        onValueChange = { exercise.apply { update(value.copy(name = it)) } },
        keyboardOptions = KeyboardOptions(
          imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Sentences
        ),
        modifier = Modifier.fillMaxWidth()
      )
      IntNumberField(
        value = exercise.value.restDuration.inWholeMinutes.toInt(),
        label = { Text(stringResource(R.string.label_rest_duration_optional)) },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        onValueChange = { exercise.apply { update(value.copy(restDuration = it.minutes)) } },
        suffix = { Text(stringResource(R.string.suffix_minutes)) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth()
      )
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(stringResource(R.string.label_timer))
          Checkbox(checked = uiState.hasTimer.value, onCheckedChange = {
            uiState.hasTimer.update(it)
          })
        }
        TextField(
          enabled = !uiState.hasTimer.value,
          value = uiState.setUnit.value,
          label = { Text(stringResource(R.string.label_set_unit)) },
          shape = RectangleShape,
          colors = borderlessTextFieldColors(),
          onValueChange = { uiState.setUnit.update(it) },
          keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.None
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      FormList(
        title = stringResource(R.string.title_sets),
        onAdd = { uiState.addSet() },
        modifier = Modifier.fillMaxWidth()
      ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp)) {
          itemsIndexed(items = sets.values, key = { _, set -> set.first }) { i, set ->
            val dens = LocalDensity.current
            val dismissState = rememberSwipeToDismissBoxState(
              positionalThreshold = { with(dens) { 70.dp.toPx() } })
            val deleteDirection = SwipeToDismissBoxValue.StartToEnd

            LaunchedEffect(dismissState.currentValue) {
              if (dismissState.currentValue == deleteDirection) {
                sets.remove(set)
              }
            }

            SwipeToDismissBox(
              state = dismissState,
              enableDismissFromStartToEnd = true,
              enableDismissFromEndToStart = false,
              backgroundContent = {
                DismissBoxBackgroundContent(state = dismissState, deleteDirection = deleteDirection)
              },
              modifier = Modifier.animateItem()
            ) {
              IntNumberField(
                value = set.second.value.value,
                shape = RectangleShape,
                colors = TextFieldDefaults.colors(
                  unfocusedIndicatorColor = Color.Transparent,
                  focusedIndicatorColor = Color.Transparent,
                  unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline,
                  focusedPlaceholderColor = MaterialTheme.colorScheme.outline
                ),
                placeholder = { Text(stringResource(R.string.ph_amount)) },
                onValueChange = { value -> set.second.update(set.second.value.copy(value = value)) },
                suffix = { Text(uiState.setUnit.value) },
                keyboardOptions = KeyboardOptions(
                  imeAction = if (i < sets.values.count() - 1) ImeAction.Next else ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun UiStateEffects(
  uiState: ExerciseFormViewModel.UiState, onDeleted: () -> Unit, onSaved: (Long) -> Unit
) {
  LaunchedEffect(uiState.deleted) {
    if (uiState.deleted) {
      onDeleted()
    }
  }

  LaunchedEffect(uiState.savingState.state) {
    if (uiState.savingState.state == SavingState.State.SAVED) {
      onSaved(uiState.exercise.value.id)
    }
  }
}

@Composable
private fun DismissBoxBackgroundContent(
  state: SwipeToDismissBoxState, deleteDirection: SwipeToDismissBoxValue
) {
  val color = when (state.targetValue) {
    deleteDirection -> MaterialTheme.colorScheme.errorContainer
    else -> Color.Transparent
  }
  val icon = when (state.dismissDirection) {
    deleteDirection -> Icons.Filled.Delete
    else -> null
  }
  val alignment = when (deleteDirection) {
    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
    else -> Alignment.CenterStart
  }

  Box(
    contentAlignment = alignment, modifier = Modifier
      .fillMaxSize()
      .background(color)
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.minimumInteractiveComponentSize()
      )
    }
  }
}

@Composable
private fun DeleteDialog(
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) {
  AlertDialog(
    title = { Text(text = stringResource(R.string.dialog_title_delete_exercise)) },
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(
        onClick = onConfirm,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
      ) {
        Text(text = stringResource(R.string.btn_delete))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = stringResource(R.string.btn_cancel))
      }
    },
  )
}