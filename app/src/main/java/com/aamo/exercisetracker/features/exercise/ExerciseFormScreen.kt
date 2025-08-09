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
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.DeleteDialog
import com.aamo.exercisetracker.ui.components.FormList
import com.aamo.exercisetracker.ui.components.IntNumberField
import com.aamo.exercisetracker.ui.components.LoadingIconButton
import com.aamo.exercisetracker.ui.components.UnsavedDialog
import com.aamo.exercisetracker.ui.components.borderlessTextFieldColors
import com.aamo.exercisetracker.utility.extensions.form.HideZero
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.extensions.general.letIf
import com.aamo.exercisetracker.utility.extensions.general.onTrue
import com.aamo.exercisetracker.utility.viewmodels.SavingState
import com.aamo.exercisetracker.utility.viewmodels.ViewModelState
import com.aamo.exercisetracker.utility.viewmodels.ViewModelStateList
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Serializable
data class EditExerciseFormScreen(val id: Long)
@Serializable
data class AddExerciseFormScreen(val routineId: Long)

class ExerciseFormViewModel(
  private val fetchData: suspend () -> Model,
  private val saveData: suspend (Model) -> Boolean,
  private val deleteData: suspend () -> Boolean,
) : ViewModel() {
  data class Model(
    val exerciseName: String,
    val restDuration: Duration,
    val setUnit: String,
    val setAmounts: List<Int>,
    val hasTimer: Boolean,
    val isNew: Boolean,
  )

  class UiState {
    inner class SetAmount(value: Int = 0) {
      val amount = ViewModelState(value).validation { validation(it) }
      val listKey: Int = nextSetKey++

      private fun validation(value: Int): Int {
        return value.letIf(hasTimer.value) {
          it.minutes.inWholeMilliseconds.coerceAtMost(Int.MAX_VALUE.toLong()).milliseconds.inWholeMinutes.toInt()
        }
      }
    }

    val exerciseName = ViewModelState(String.EMPTY).onChange { onUnsavedChanges() }
    val restDuration = ViewModelState(0.milliseconds).onChange { onUnsavedChanges() }
    val setUnit = ViewModelState(String.EMPTY).onChange { onUnsavedChanges() }
    val sets = ViewModelStateList<SetAmount>().onChange { onUnsavedChanges() }
    var hasTimer = ViewModelState(false).onChange {
      // Revalidate set amounts
      sets.values.forEach { it.amount.update(it.amount.value) }
      onUnsavedChanges()
    }
    var isNew by mutableStateOf(false)
    var savingState by mutableStateOf(SavingState(canSave = { canSave() }))

    private var nextSetKey = 0 // Used for set amount list

    fun addSet() {
      sets.add(SetAmount())
    }

    private fun onUnsavedChanges() {
      if (!savingState.unsavedChanges) {
        savingState = savingState.copy(unsavedChanges = true)
      }
    }

    private fun canSave(): Boolean {
      return when {
        savingState.state == SavingState.State.SAVING -> false
        sets.values.isEmpty() -> false
        sets.values.any { it.amount.value == 0 } -> false
        else -> true
      }
    }
  }

  val uiState = UiState()

  init {
    viewModelScope.launch {
      fetchData().also { result ->
        uiState.apply {
          exerciseName.update(result.exerciseName)
          restDuration.update(result.restDuration)
          sets.add(*result.setAmounts.map { SetAmount(it) }.toTypedArray())
          setUnit.update(result.setUnit)
          hasTimer.update(result.hasTimer)
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
            exerciseName = s.exerciseName.value,
            restDuration = s.restDuration.value,
            setUnit = s.setUnit.value,
            setAmounts = s.sets.values.map { it.amount.value },
            hasTimer = s.hasTimer.value,
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

  /**
   * Deletes the exercise from the database
   */
  fun delete() {
    if (uiState.isNew) return

    viewModelScope.launch {
      runCatching { deleteData() }
    }
  }
}

// TODO: Combine graphs to one

fun NavGraphBuilder.addExerciseFormScreen(onBack: () -> Unit, onSaved: (id: Long) -> Unit) {
  composable<AddExerciseFormScreen> { navStack ->
    val (routineId) = navStack.toRoute<AddExerciseFormScreen>()
    val dao = RoutineDatabase.getDatabase(LocalContext.current.applicationContext).routineDao()
    val setUnitDefault = stringResource(R.string.ph_reps)
    val viewmodel: ExerciseFormViewModel = viewModel(factory = viewModelFactory {
      initializer {
        ExerciseFormViewModel(fetchData = {
          ExerciseFormViewModel.Model(
            exerciseName = String.EMPTY,
            restDuration = 0.milliseconds,
            setUnit = setUnitDefault,
            setAmounts = listOf(0),
            hasTimer = false,
            isNew = true
          )
        }, saveData = { model ->
          dao.upsert(
            ExerciseWithSets(
              exercise = Exercise(
                routineId = routineId, name = model.exerciseName, restDuration = model.restDuration
              ), sets = model.setAmounts.map { amount ->
                ExerciseSet(
                  value = amount.letIf(model.hasTimer) {
                    // Change minutes to milliseconds if set has timer
                    it.minutes.inWholeMilliseconds.toInt()
                  }, unit = model.setUnit, valueType = ifElse(
                    condition = model.hasTimer,
                    ifTrue = { ExerciseSet.ValueType.COUNTDOWN },
                    ifFalse = { ExerciseSet.ValueType.REPETITION }), exerciseId = 0L
                )
              })
          ).let { result ->
            (result > 0).onTrue {
              onSaved(result)
            }
          }
        }, deleteData = { false })
      }
    })
    val uiState = viewmodel.uiState

    ExerciseFormScreen(
      uiState = uiState,
      onBack = onBack,
      onSave = { viewmodel.save() },
      onDelete = { viewmodel.delete() })
  }
}

fun NavGraphBuilder.editExerciseFormScreen(
  onBack: () -> Unit, onSaved: (id: Long) -> Unit, onDeleted: () -> Unit
) {
  composable<EditExerciseFormScreen> { navStack ->
    val (exerciseId) = navStack.toRoute<EditExerciseFormScreen>()
    val dao = RoutineDatabase.getDatabase(LocalContext.current.applicationContext).routineDao()
    val setUnitDefault = stringResource(R.string.ph_reps)
    val viewmodel: ExerciseFormViewModel = viewModel(factory = viewModelFactory {
      initializer {
        ExerciseFormViewModel(fetchData = {
          (dao.getExerciseWithSets(exerciseId)
            ?: throw Exception("Failed to fetch data")).let { (exercise, sets) ->
            ExerciseFormViewModel.Model(
              exerciseName = exercise.name,
              restDuration = exercise.restDuration,
              setUnit = sets.firstOrNull()?.unit ?: setUnitDefault,
              setAmounts = sets.map { set ->
                set.value.letIf(set.valueType == ExerciseSet.ValueType.COUNTDOWN) {
                  // Change milliseconds to minutes, if set has timer
                  it.milliseconds.inWholeMinutes.toInt()
                }
              },
              hasTimer = (sets.firstOrNull()?.valueType == ExerciseSet.ValueType.COUNTDOWN),
              isNew = false
            )
          }
        }, saveData = { model ->
          (dao.getExerciseWithSets(exerciseId)
            ?: throw Exception("Failed to fetch data")).let { (exercise, sets) ->
            dao.upsert(
              ExerciseWithSets(
                exercise = exercise.copy(
                  name = model.exerciseName, restDuration = model.restDuration
                ), sets = sets.take(model.setAmounts.size).let { list ->
                  list.toMutableList().apply {
                    // Add missing sets
                    repeat(model.setAmounts.size - list.size) { add(ExerciseSet(exerciseId = exerciseId)) }
                  }.let { list ->
                    list.mapIndexed { i, item ->
                      item.copy(
                        exerciseId = exercise.id,
                        value = model.setAmounts[i].letIf(model.hasTimer) {
                          // Change minutes to milliseconds if set has timer
                          it.minutes.inWholeMilliseconds.toInt()
                        },
                        unit = model.setUnit,
                        valueType = ifElse(
                          condition = model.hasTimer,
                          ifTrue = { ExerciseSet.ValueType.COUNTDOWN },
                          ifFalse = { ExerciseSet.ValueType.REPETITION })
                      )
                    }
                  }
                })
            ).let { result ->
              (result > 0).onTrue {
                onSaved(exerciseId)
              }
            }
          }
        }, deleteData = {
          (dao.getExercise(exerciseId) ?: throw Exception("Failed to fetch data")).let { result ->
            (dao.delete(result) > 0).onTrue {
              onDeleted()
            }
          }
        })
      }
    })
    val uiState = viewmodel.uiState

    ExerciseFormScreen(
      uiState = uiState,
      onBack = onBack,
      onSave = { viewmodel.save() },
      onDelete = { viewmodel.delete() })
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseFormScreen(
  uiState: ExerciseFormViewModel.UiState,
  onBack: () -> Unit,
  onSave: () -> Unit,
  onDelete: () -> Unit,
) {
  val defaultSetTimerUnit = stringResource(R.string.ph_minutes)

  var openDeleteDialog by remember { mutableStateOf(false) }
  var openUnsavedDialog by remember { mutableStateOf(false) }

  LaunchedEffect(uiState.hasTimer.value) {
    if (uiState.hasTimer.value) {
      // Set set unit to default timer unit
      uiState.setUnit.update(defaultSetTimerUnit)
    }
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
      title = stringResource(R.string.dialog_title_delete_exercise),
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
          ifTrue = { stringResource(R.string.title_new_exercise) },
          ifFalse = { stringResource(R.string.title_edit_exercise) })
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
        value = uiState.exerciseName.value,
        label = { Text(stringResource(R.string.label_name)) },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        onValueChange = { uiState.exerciseName.update(it) },
        keyboardOptions = KeyboardOptions(
          imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Sentences
        ),
        modifier = Modifier.fillMaxWidth()
      )
      IntNumberField(
        value = uiState.restDuration.value.inWholeMinutes.toInt(),
        label = { Text(stringResource(R.string.label_rest_duration_optional)) },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        onValueChange = { uiState.restDuration.update(it.minutes) },
        suffix = { Text(stringResource(R.string.suffix_minutes)) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        visualTransformation = VisualTransformation.HideZero,
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
          itemsIndexed(
            items = uiState.sets.values, key = { _, set -> set.listKey }) { i, set ->
            val density = LocalDensity.current
            val dismissState = rememberSwipeToDismissBoxState(
              positionalThreshold = { with(density) { 70.dp.toPx() } })
            val deleteDirection = SwipeToDismissBoxValue.StartToEnd

            LaunchedEffect(dismissState.currentValue) {
              if (dismissState.currentValue == deleteDirection) {
                uiState.sets.remove(set)
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
                value = set.amount.value,
                shape = RectangleShape,
                colors = TextFieldDefaults.colors(
                  unfocusedIndicatorColor = Color.Transparent,
                  focusedIndicatorColor = Color.Transparent,
                  unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline,
                  focusedPlaceholderColor = MaterialTheme.colorScheme.outline
                ),
                placeholder = { Text(stringResource(R.string.ph_amount)) },
                onValueChange = { set.amount.update(it) },
                suffix = { Text(uiState.setUnit.value) },
                keyboardOptions = KeyboardOptions(
                  imeAction = ifElse(
                    condition = i < uiState.sets.values.count() - 1,
                    ifTrue = { ImeAction.Next },
                    ifFalse = { ImeAction.Done })
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