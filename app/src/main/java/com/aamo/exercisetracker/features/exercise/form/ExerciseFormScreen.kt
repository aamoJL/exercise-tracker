package com.aamo.exercisetracker.features.exercise.form

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
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
import com.aamo.exercisetracker.database.entities.Exercise
import com.aamo.exercisetracker.database.entities.ExerciseSet
import com.aamo.exercisetracker.database.entities.ExerciseWithSets
import com.aamo.exercisetracker.features.exercise.form.components.ExerciseSetSwipeToDismissBox
import com.aamo.exercisetracker.features.exercise.form.models.ExerciseFormFields
import com.aamo.exercisetracker.features.exercise.form.use_cases.deleteExercise
import com.aamo.exercisetracker.features.exercise.form.use_cases.fetchExercise
import com.aamo.exercisetracker.features.exercise.form.use_cases.saveExercise
import com.aamo.exercisetracker.ui.components.BackgroundSurface
import com.aamo.exercisetracker.ui.components.FormList
import com.aamo.exercisetracker.ui.components.HorizontalDividerLabel
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.components.inputs.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.inputs.LabelledCheckBox
import com.aamo.exercisetracker.ui.components.inputs.LoadingIconButton
import com.aamo.exercisetracker.ui.components.inputs.number_field.DurationNumberField
import com.aamo.exercisetracker.ui.components.inputs.number_field.DurationNumberFieldFields
import com.aamo.exercisetracker.ui.components.inputs.text_field.borderlessTextFieldColors
import com.aamo.exercisetracker.ui.components.modals.DeleteDialog
import com.aamo.exercisetracker.ui.components.modals.UnsavedDialog
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.aamo.exercisetracker.utility.extensions.form.getNewUUID
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import com.aamo.exercisetracker.utility.viewmodels.SavingState
import com.aamo.exercisetracker.utility.viewmodels.ViewModelState
import com.aamo.exercisetracker.utility.viewmodels.ViewModelStateList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Serializable
data class ExerciseFormScreen(val exerciseId: Long, val routineId: Long)

class ExerciseFormViewModel(
  private val fetchData: suspend () -> ExerciseWithSets?,
  private val saveData: suspend (ExerciseWithSets) -> Unit,
  private val deleteData: suspend (Exercise) -> Unit,
) : ViewModel() {

  class FormState(fields: ExerciseFormFields) {
    inner class SetValue(value: Int) {
      val uuid: UUID = getNewUUID(used = setValues.values.map { it.uuid })
      val value = ViewModelState(value).onChange { onUnsavedChanges() }
    }

    val exerciseName = ViewModelState(fields.name).onChange { onUnsavedChanges() }
    val restDuration = ViewModelState(fields.restDuration).onChange { onUnsavedChanges() }
    val setUnit = ViewModelState(fields.unit).onChange { onUnsavedChanges() }
    val setValues = ViewModelStateList<SetValue>().onChange { onUnsavedChanges() }
    var hasTimer = ViewModelState(fields.hasTimer).onChange { onUnsavedChanges() }
    var hasRest = ViewModelState(fields.hasRest).onChange { onUnsavedChanges() }
    val isNew = fields.name.isEmpty()
    var savingState by mutableStateOf(SavingState())

    init {
      // this needs to be here instead of in the ViewModelStateList parameters, otherwise
      //  preview will not build
      setValues.add(*fields.setValues.map { SetValue(it) }.toTypedArray())
      savingState = SavingState()
    }

    fun addSetValue() {
      setValues.add(SetValue(0))
    }

    private fun onUnsavedChanges() {
      if (!savingState.unsavedChanges) {
        savingState = savingState.copy(unsavedChanges = true)
      }
    }

    fun canSave(): Boolean {
      if (savingState.state == SavingState.State.SAVING) return false
      if (exerciseName.value.isEmpty()) return false
      if (!hasTimer.value && setUnit.value.isEmpty()) return false
      if (setValues.values.isEmpty()) return false
      if (setValues.values.any { it.value.value == 0 }) return false
      if (hasRest.value && restDuration.value <= 0.seconds) return false
      return true
    }
  }

  private var model: Exercise? = null

  private val _formState = MutableSharedFlow<FormState?>().onStart {
    val (exercise, sets) = fetchData() ?: throw IllegalStateException("Failed to fetch data")
    model = exercise

    emit(
      FormState(
        fields = ExerciseFormFields(
          name = exercise.name,
          restDuration = exercise.restDuration,
          unit = sets.firstOrNull()?.unit ?: String.EMPTY,
          setValues = sets.map { it.value },
          hasTimer = sets.firstOrNull()?.valueType == ExerciseSet.ValueType.COUNTDOWN,
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
          ExerciseWithSets(
            exercise = checkNotNull(model).copy(
              name = s.exerciseName.value, restDuration = s.restDuration.value
            ), sets = s.setValues.values.map { set ->
              ExerciseSet(
                exerciseId = checkNotNull(model).id,
                value = set.value.value,
                unit = s.setUnit.value,
                valueType = if (s.hasTimer.value) ExerciseSet.ValueType.COUNTDOWN
                else ExerciseSet.ValueType.REPETITION
              )
            })
        })
      }.onSuccess { _ ->
        formState.apply { savingState = savingState.getAsSaved() }
      }.onFailure { error ->
        formState.apply { savingState = savingState.getAsError(error = Error(error)) }
      }
    }
  }

  /**
   * Deletes the exercise from the database
   */
  fun delete() {
    val formState = formState.value ?: return
    if (formState.isNew) return

    viewModelScope.launch {
      runCatching { deleteData(checkNotNull(model)) }
    }
  }
}

fun NavGraphBuilder.exerciseFormScreen(
  onBack: () -> Unit, onUpdate: (id: Long) -> Unit, onAdd: (id: Long) -> Unit, onDeleted: () -> Unit
) {
  composable<ExerciseFormScreen> { navStack ->
    val (exerciseId, routineId) = navStack.toRoute<ExerciseFormScreen>()
    val dao = RoutineDatabase.getDatabase(LocalContext.current.applicationContext).routineDao()
    val defaultUnit = stringResource(R.string.default_repetitions_unit)
    val viewmodel: ExerciseFormViewModel = viewModel(factory = viewModelFactory {
      initializer {
        ExerciseFormViewModel(fetchData = {
          fetchExercise(
            dao = dao, exerciseId = exerciseId, routineId = routineId, defaultUnit = defaultUnit
          )
        }, saveData = { model ->
          saveExercise(dao = dao, model = model).also {
            if (model.exercise.id == 0L) onAdd(it) else onUpdate(it)
          }
        }, deleteData = { model ->
          deleteExercise(dao = dao, model = model).also {
            onDeleted()
          }
        })
      }
    })
    val formState by viewmodel.formState.collectAsStateWithLifecycle()

    LoadingScreen(model = formState) {
      ExerciseFormScreenContent(
        formState = it,
        onBack = onBack,
        onSave = { viewmodel.save() },
        onDelete = { viewmodel.delete() })
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseFormScreenContent(
  formState: ExerciseFormViewModel.FormState,
  onBack: () -> Unit,
  onSave: () -> Unit,
  onDelete: () -> Unit,
) {
  var openDeleteDialog by remember { mutableStateOf(false) }
  var openUnsavedDialog by remember { mutableStateOf(false) }

  LaunchedEffect(formState.hasTimer.value) {
    if (formState.hasTimer.value) {
      formState.setUnit.update(String.EMPTY)
    }
  }

  LaunchedEffect(formState.hasRest.value) {
    if (!formState.hasRest.value) {
      formState.restDuration.update(0.seconds)
    }
  }

  UnsavedDialog(
    open = openUnsavedDialog, onDismiss = { openUnsavedDialog = false },
    onConfirm = {
      openUnsavedDialog = false
      onBack()
    },
  )
  DeleteDialog(
    open = openDeleteDialog,
    title = stringResource(R.string.dialog_title_delete_exercise),
    onDismiss = { openDeleteDialog = false },
    onConfirm = {
      openDeleteDialog = false
      onDelete()
    })

  BackHandler(enabled = formState.savingState.unsavedChanges) {
    openUnsavedDialog = true
  }

  Scaffold(topBar = {
    TopAppBar(title = {
      Text(
        text = ifElse(
          condition = formState.isNew,
          ifTrue = { stringResource(R.string.title_new_exercise) },
          ifFalse = { stringResource(R.string.title_existing_exercise) })
      )
    }, navigationIcon = {
      BackNavigationIconButton(onBack = {
        if (formState.savingState.unsavedChanges) openUnsavedDialog = true else onBack()
      })
    }, actions = {
      if (!formState.isNew) {
        IconButton(onClick = { openDeleteDialog = true }) {
          Icon(
            painter = painterResource(R.drawable.rounded_delete_24),
            contentDescription = stringResource(R.string.cd_delete_exercise)
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
          contentDescription = stringResource(R.string.cd_save_exercise)
        )
      }
    })
  }) { innerPadding ->
    BackgroundSurface(modifier = Modifier.fillMaxSize()) {
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
          .padding(innerPadding)
          .padding(8.dp)
      ) {
        ExerciseInfo(formState = formState)
        FormList(
          title = stringResource(R.string.title_sets),
          actions = {
            OutlinedIconButton(onClick = { formState.addSetValue() }) {
              Icon(
                painter = painterResource(R.drawable.rounded_add_24),
                contentDescription = stringResource(R.string.cd_add_new_item),
              )
            }
          },
          modifier = Modifier.fillMaxWidth(),
        ) {
          LazyColumn {
            itemsIndexed(
              items = formState.setValues.values, key = { _, set -> set.uuid }) { i, set ->
              Column(modifier = Modifier.animateItem()) {
                ExerciseSetSwipeToDismissBox(
                  value = set.value.value,
                  isDuration = formState.hasTimer.value,
                  suffix = formState.setUnit.value,
                  imeAction = ifElse(
                    condition = i < formState.setValues.values.size - 1,
                    ifTrue = { ImeAction.Next },
                    ifFalse = { ImeAction.Done }),
                  onChange = { set.value.update(it) },
                  onRemove = { formState.setValues.remove(set) },
                )
                if (i != formState.setValues.values.size - 1) {
                  HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ExerciseInfo(
  formState: ExerciseFormViewModel.FormState, modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    HorizontalDividerLabel(
      label = stringResource(R.string.label_exercise), modifier = Modifier.padding(12.dp)
    )
    ElevatedCard(shape = MaterialTheme.shapes.small) {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .padding(8.dp)
          .fillMaxWidth()
      ) {
        TextField(
          value = formState.exerciseName.value,
          label = { Text(stringResource(R.string.label_name)) },
          shape = RectangleShape,
          colors = borderlessTextFieldColors(),
          onValueChange = { formState.exerciseName.update(it) },
          keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Sentences
          ),
          modifier = Modifier.fillMaxWidth()
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          LabelledCheckBox(
            checked = formState.hasTimer.value,
            onCheckedChange = { formState.hasTimer.update(it) },
            label = { Text(stringResource(R.string.label_timer)) },
          )
          TextField(
            enabled = !formState.hasTimer.value,
            value = formState.setUnit.value,
            label = { Text(stringResource(R.string.label_set_unit)) },
            shape = RectangleShape,
            colors = borderlessTextFieldColors(),
            onValueChange = { formState.setUnit.update(it) },
            keyboardOptions = KeyboardOptions(
              imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.None
            ),
          )
        }
        Surface(
          color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
          shape = MaterialTheme.shapes.extraSmall,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            LabelledCheckBox(
              checked = formState.hasRest.value,
              onCheckedChange = { formState.hasRest.update(it) },
              label = { Text(stringResource(R.string.label_rest_duration)) },
              modifier = Modifier
            )
            DurationNumberField(
              enabled = formState.hasRest.value,
              value = formState.restDuration.value,
              onValueChange = { formState.restDuration.update(it) },
              hideZeroOnDisabled = true,
              fields = DurationNumberFieldFields(
                hours = DurationNumberFieldFields.Properties(enabled = false)
              ),
              shape = RectangleShape,
              colors = borderlessTextFieldColors(),
              modifier = Modifier.padding(4.dp)
            )
          }
        }
      }
    }
  }
}

@Suppress("HardCodedStringLiteral")
@PreviewLightDark
@Composable
private fun PreviewRepetitions() {
  ExerciseTrackerTheme {
    ExerciseFormScreenContent(
      formState = ExerciseFormViewModel.FormState(
      fields = ExerciseFormFields(
        name = "Exercise 1",
        restDuration = 0.minutes,
        unit = "Reps",
        setValues = listOf(1, 2, 3),
        hasTimer = false,
      )
    ), onBack = {}, onSave = {}, onDelete = {})
  }
}

@Suppress("HardCodedStringLiteral")
@PreviewLightDark
@Composable
private fun PreviewTimer() {
  ExerciseTrackerTheme {
    ExerciseFormScreenContent(
      formState = ExerciseFormViewModel.FormState(
      fields = ExerciseFormFields(
        name = "Exercise 1", restDuration = 3.minutes, unit = String.EMPTY, setValues = listOf(
          30.seconds.inWholeMilliseconds.toInt(), 2.minutes.inWholeMilliseconds.toInt()
        ), hasTimer = true
      )
    ), onBack = {}, onSave = {}, onDelete = {})
  }
}