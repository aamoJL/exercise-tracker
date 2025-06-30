package com.aamo.exercisetracker.features.exercise

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.utility.extensions.general.onNotNull
import com.aamo.exercisetracker.utility.extensions.general.onNull
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Serializable
data class ExerciseScreen(val id: Long = 0)

class ExerciseScreenViewModel(
  private val exerciseId: Long,
  private val routineDao: RoutineDao,
) : ViewModel() {
  data class SetState(
    val set: ExerciseSet? = null,
    val index: Int = 0,
    val total: Int = 0,
  )

  data class RestState(
    val isResting: Boolean = false,
    val restDuration: Duration = 0.milliseconds,
    val remainingDuration: Duration = 0.milliseconds
  )

  private var restTimer: CountDownTimer? = null

  var exercise: ExerciseWithSets? by mutableStateOf(null)
    private set
  var setState by mutableStateOf(SetState())
    private set
  var restState by mutableStateOf(RestState())
    private set
  var isLoading by mutableStateOf(true)
    private set

  init {
    viewModelScope.launch {
      // TODO: change to flow? so edit will apply
      routineDao.getExerciseWithSets(exerciseId).let {
        exercise = it
        setState = SetState(
          set = it?.sets?.firstOrNull(), index = 0, total = it?.sets?.size ?: 0
        )
        restState = RestState(restDuration = it?.exercise?.restDuration ?: 0.seconds)
        isLoading = false
      }
    }
  }

  fun setCompleted() {
    val nextIndex = Math.clamp((setState.index + 1).toLong(), 0, setState.total)

    exercise?.sets?.elementAtOrNull(nextIndex).onNull {
      setState = setState.copy(set = null, index = nextIndex)
    }.onNotNull { nextSet ->
      restState = restState.copy(isResting = true)

      restTimer?.cancel()
      restTimer = object : CountDownTimer(
        restState.restDuration.inWholeMilliseconds, 1.seconds.inWholeMilliseconds
      ) {
        override fun onTick(millisUntilFinished: Long) {
          runCatching { checkNotNull(restTimer) }.onFailure {
            Log.e("asd", it.message.toString())
          }

          restState = restState.copy(remainingDuration = millisUntilFinished.milliseconds)
        }

        override fun onFinish() {
          cancel()
          restTimer = null
          restState = restState.copy(isResting = false)
          setState = setState.copy(set = nextSet, index = nextIndex)
        }
      }.start()
    }
  }

  fun stopRest() {
    restTimer?.onFinish()
  }
}

fun NavGraphBuilder.exerciseScreen(onBack: () -> Unit, onEdit: (id: Long) -> Unit) {
  composable<ExerciseScreen> { navStack ->
    val (id) = navStack.toRoute<ExerciseScreen>()
    val context = LocalContext.current.applicationContext
    val viewmodel: ExerciseScreenViewModel = viewModel(factory = viewModelFactory {
      initializer {
        ExerciseScreenViewModel(
          exerciseId = id,
          routineDao = RoutineDatabase.getDatabase(context).routineDao(),
        )
      }
    })
    val exercise = viewmodel.exercise?.exercise
    val setState = viewmodel.setState
    val restState = viewmodel.restState

    LoadingScreen(enabled = viewmodel.isLoading) {
      if (exercise != null) {
        ExerciseScreen(
          exercise = exercise,
          setState = setState,
          restState = restState,
          onBack = onBack,
          onEdit = { onEdit(id) },
          onSetCompleted = {
            viewmodel.setCompleted()
          },
          onStopRest = { viewmodel.stopRest() })
      }
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ExerciseScreen(
  exercise: Exercise,
  setState: ExerciseScreenViewModel.SetState,
  restState: ExerciseScreenViewModel.RestState,
  onBack: () -> Unit,
  onEdit: () -> Unit,
  onSetCompleted: () -> Unit,
  onStopRest: () -> Unit,
) {
  val restSheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true,
    confirmValueChange = { false /* Prevents closing by pressing outside the sheet */ })
  val set = remember(setState.set) { setState.set }
  var showRestSheet by remember { mutableStateOf(false) }
  //var restProgress by remember { mutableFloatStateOf(0f) } /* TODO: Timer progress */
  //var exerciseProgress by remember { mutableFloatStateOf(0f) } /* TODO: Exercise progress */

  // TODO: back handling, remember also edit

  LaunchedEffect(restState.isResting) {
    restSheetState.apply {
      if (restState.isResting) show() else hide()
    }

    // sheet state can't be checked with SheetState.currentValue, because confirmValueChange
    //  is always false, so the state will not change when closing the sheet.
    showRestSheet = restState.isResting
  }

  Scaffold(
    topBar = {
      TopAppBar(title = { Text(exercise.name) }, actions = {
        IconButton(onClick = onEdit) {
          Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit exercise")
        }
      }, navigationIcon = { BackNavigationIconButton(onBack = onBack) })
    }) { innerPadding ->
    Surface(
      modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize()
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.padding(16.dp)
      ) {
        SetProgress(current = setState.index + 1, total = setState.total)
        if (set != null) {
          SetContent(set = set, isResting = restState.isResting, onDone = onSetCompleted)
        }
      }
    }

    // Visibility needs to be checked with showRestSheet instead of setState.isResting because the
    //  sheet closing animation will not work correctly.
    RestSheet(
      isVisible = showRestSheet,
      restState = restState,
      sheetState = restSheetState,
      onDismissRequest = onStopRest,
    )
  }
}

@Composable
fun SetProgress(current: Int, total: Int) {
  val percent = (current.toFloat() - 1) / total.toFloat()

  Box(
    contentAlignment = Alignment.Center, modifier = Modifier
      .fillMaxWidth(.7f)
      .aspectRatio(1f)
  ) {
    /* TODO: Segmented progress indicator */
    CircularProgressIndicator(
      progress = { percent },
      strokeWidth = 20.dp,
      gapSize = 0.dp,
      strokeCap = StrokeCap.Butt,
      modifier = Modifier.fillMaxSize()
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      if (current > total) {
        Text("Completed", style = MaterialTheme.typography.titleMedium)
      }
      else {
        Text("Set", style = MaterialTheme.typography.titleMedium)
      }
      Text(
        text = "${min(current, total)}/$total",
        style = MaterialTheme.typography.displayLarge,
        textAlign = TextAlign.Center
      )
      Spacer(Modifier.height(with(LocalDensity.current) {
        // Centers the text inside the progress indicator
        MaterialTheme.typography.titleMedium.lineHeight.toDp()
      }))
    }
  }
}

@Composable
fun SetContent(set: ExerciseSet, isResting: Boolean, onDone: () -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Card {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .padding(24.dp)
          .fillMaxWidth()
      ) {
        Text("Current set", style = MaterialTheme.typography.titleMedium)
        Text("${set.value} ${set.unit}", style = MaterialTheme.typography.displayMedium)
      }
    }
    Button(
      enabled = !isResting,
      shape = CardDefaults.shape,
      onClick = onDone,
      modifier = Modifier
        .heightIn(max = 100.dp)
        .fillMaxSize()
    ) {
      Text("Done", style = MaterialTheme.typography.titleLarge)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestSheet(
  isVisible: Boolean,
  restState: ExerciseScreenViewModel.RestState,
  sheetState: SheetState,
  onDismissRequest: () -> Unit
) {
  if (isVisible) {
    val progress =
      if (restState.restDuration == 0.seconds) 0f else 1f - (restState.remainingDuration / restState.restDuration).toFloat()

    ModalBottomSheet(
      sheetState = sheetState, onDismissRequest = onDismissRequest, dragHandle = null
    ) {
      Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight(.8f)
          .pointerInput(Unit) {
            detectVerticalDragGestures(onVerticalDrag = { change, _ -> change.consume() })
            detectTapGestures(onPress = {})
          }) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .fillMaxHeight(.8f)
            .fillMaxWidth(.7f)
            .aspectRatio(1f)
        ) {
          CircularProgressIndicator(
            progress = { progress },
            strokeWidth = 20.dp,
            gapSize = 0.dp,
            strokeCap = StrokeCap.Butt,
            modifier = Modifier.fillMaxSize()
          )
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Rest", style = MaterialTheme.typography.titleMedium)
            Text(
              text = "${
                restState.remainingDuration.inWholeMinutes.toString().padStart(2, '0')
              }:${(restState.remainingDuration.inWholeSeconds % 60).toString().padStart(2, '0')}",
              style = MaterialTheme.typography.displayLarge,
              textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(with(LocalDensity.current) {
              // Centers the text inside the progress indicator
              MaterialTheme.typography.titleMedium.lineHeight.toDp()
            }))
          }
        }
        Button(onClick = onDismissRequest) {
          Text("Stop")
        }
      }
    }
  }
}