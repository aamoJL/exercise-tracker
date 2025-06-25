package com.aamo.exercisetracker.features.exercise

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
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
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.database.entities.RoutineDao
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.FormList
import com.aamo.exercisetracker.ui.components.LoadingIconButton
import com.aamo.exercisetracker.ui.components.UnsavedDialog
import com.aamo.exercisetracker.ui.components.borderlessTextFieldColors
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import com.aamo.exercisetracker.utility.viewmodels.SavingState
import com.aamo.exercisetracker.utility.viewmodels.ViewModelState
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class EditExerciseFormScreen(val id: Long)

@Serializable
data class AddExerciseFormScreen(val routineId: Long)

class ExerciseFormViewModel(
  context: Context, fetchData: (suspend (RoutineDao) -> ExerciseWithSets?)?
) : ViewModel() {
  class UiState(onModelChanged: () -> Unit) {
    val exercise = ViewModelState(Exercise()).onChange { onModelChanged() }
    /**
     * Pair or exercise set and its list key
     */
    val sets = ViewModelState(listOf(Pair(0, ExerciseSet()))).onChange { onModelChanged() }
    var savingState by mutableStateOf(SavingState())
    var deleted by mutableStateOf(false)
    var nextSetKey by mutableIntStateOf(1)
  }

  private val database: RoutineDatabase = RoutineDatabase.getDatabase(context)

  val uiState = UiState(onModelChanged = { onModelChanged() })

  init {
    viewModelScope.launch {
      if (fetchData != null) {
        fetchData(database.routineDao())?.let { ews ->
          var nextKey = uiState.nextSetKey
          uiState.exercise.update(ews.exercise)
          uiState.sets.update(ews.sets.map { set -> Pair(nextKey, set).also { nextKey++ } })
          uiState.savingState = SavingState(canSave = canSave())
          uiState.nextSetKey = nextKey
        }
      }
    }
  }

  fun save() {
    if (!canSave()) return

    val exercise = uiState.exercise.value
    val sets = uiState.sets.value

    uiState.apply { savingState = savingState.getAsSaving() }

    viewModelScope.launch {
      database.routineDao().runCatching {
        checkNotNull(
          upsertAndGet(ExerciseWithSets(exercise = exercise, sets = sets.map { it.second }))
        )
      }.onSuccess { result ->
        var nextKey = uiState.nextSetKey

        uiState.exercise.update(result.exercise)
        uiState.sets.update(result.sets.map { set -> Pair(nextKey, set).also { nextKey++ } })
        uiState.apply { savingState = savingState.getAsSaved(canSave()) }
        uiState.nextSetKey = nextKey
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
      database.routineDao().runCatching {
        delete(uiState.exercise.value)
      }.onSuccess {
        uiState.deleted = true
      }
    }
  }

  private fun canSave(): Boolean {
    if (uiState.exercise.value.routineId == 0L) return false
    if (uiState.savingState.state == SavingState.State.SAVING) return false
    if (uiState.sets.value.any { it.second.value == 0 }) return false
    if (uiState.exercise.value.name.isEmpty()) return false
    if (uiState.sets.value.isEmpty()) return false
    return true
  }

  private fun onModelChanged() {
    uiState.savingState = uiState.savingState.copy(
      unsavedChanges = true, canSave = canSave()
    )
  }
}

fun NavGraphBuilder.addExerciseFormScreen(onBack: () -> Unit, onSaved: (id: Long) -> Unit) {
  composable<AddExerciseFormScreen> { navStack ->
    val (routineId) = navStack.toRoute<AddExerciseFormScreen>()

    Screen(
      fetchData = { dao ->
      ExerciseWithSets(exercise = Exercise(routineId = routineId), sets = listOf(ExerciseSet()))
    }, onBack = onBack, onSaved = onSaved)
  }
}

fun NavGraphBuilder.editExerciseFormScreen(
  onBack: () -> Unit, onSaved: (id: Long) -> Unit, onDeleted: () -> Unit
) {
  composable<EditExerciseFormScreen> { navStack ->
    val (exerciseId) = navStack.toRoute<EditExerciseFormScreen>()

    Screen(
      fetchData = { dao -> dao.getExerciseWithSets(exerciseId) },
      onBack = onBack,
      onSaved = onSaved,
      onDeleted = onDeleted
    )
  }
}

@Composable
private fun Screen(
  fetchData: (suspend (RoutineDao) -> ExerciseWithSets?)?,
  onBack: () -> Unit,
  onSaved: (id: Long) -> Unit,
  onDeleted: () -> Unit = {}
) {
  val context = LocalContext.current.applicationContext
  val viewmodel: ExerciseFormViewModel = viewModel(factory = viewModelFactory {
    initializer { ExerciseFormViewModel(context = context, fetchData = fetchData) }
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
  val focusManager = LocalFocusManager.current
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
    TopAppBar(
      title = { Text(text = if (isNew) "New Exercise" else "Edit Exercise") },
      navigationIcon = {
        BackNavigationIconButton(onBack = {
          if (unsavedChanges) openUnsavedDialog = true else onBack()
        })
      },
      actions = {
        if (!isNew) {
          IconButton(onClick = { openDeleteDialog = true }) {
            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete exercise")
          }
        }
        LoadingIconButton(
          onClick = onSave,
          isLoading = uiState.savingState.state == SavingState.State.SAVING,
          enabled = uiState.savingState.canSave
        ) {
          Icon(imageVector = Icons.Filled.Done, contentDescription = "Save exercise")
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
        value = exercise.value.name,
        label = { Text("Name") },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        onValueChange = { exercise.apply { update(value.copy(name = it)) } },
        keyboardOptions = KeyboardOptions(
          imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Sentences
        ),
        modifier = Modifier.fillMaxWidth()
      )
      TextField(
        value = exercise.value.setUnit,
        label = { Text("Set unit") },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        onValueChange = { exercise.apply { update(value.copy(setUnit = it)) } },
        keyboardOptions = KeyboardOptions(
          imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.None
        ),
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      FormList(
        title = "Sets", onAdd = {
          val nextKey = uiState.nextSetKey

          sets.apply { update(value.plus(Pair(nextKey, ExerciseSet()))) }
          uiState.nextSetKey = nextKey + 1
        }, modifier = Modifier.fillMaxWidth()
      ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp)) {
          itemsIndexed(items = sets.value, key = { _, set -> set.first }) { i, set ->
            val dens = LocalDensity.current
            val dismissState = rememberSwipeToDismissBoxState(
              positionalThreshold = { with(dens) { 70.dp.toPx() } })
            val deleteDirection = SwipeToDismissBoxValue.StartToEnd

            LaunchedEffect(dismissState.currentValue) {
              if (dismissState.currentValue == deleteDirection) {
                sets.apply { update(value.minus(set)) }
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
              TextField(
                value = if (set.second.value > 0) set.second.value.toString() else String.EMPTY,
                shape = RectangleShape,
                colors = TextFieldDefaults.colors(
                  unfocusedIndicatorColor = Color.Transparent,
                  focusedIndicatorColor = Color.Transparent,
                  unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline,
                  focusedPlaceholderColor = MaterialTheme.colorScheme.outline
                ),
                placeholder = { Text("amount...") },
                onValueChange = { value ->
                  if (value.isDigitsOnly()) {
                    val newSets = sets.value.toMutableList().also {
                      it[i] = it[i].copy(
                        second = it[i].second.copy(
                          value = if (value.isEmpty()) 0 else value.toInt()
                        )
                      )
                    }

                    sets.update(newSets)
                  }
                },
                suffix = { Text(exercise.value.setUnit) },
                keyboardOptions = KeyboardOptions(
                  keyboardType = KeyboardType.Number,
                  imeAction = if (i < sets.value.count() - 1) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                  onDone = { focusManager.clearFocus() }),
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
    title = { Text(text = "Delete exercise?") },
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