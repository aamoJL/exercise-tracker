package com.aamo.exercisetracker.features.dailies

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
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
import com.aamo.exercisetracker.database.entities.ExerciseWithProgress
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.RoutineSchedule
import com.aamo.exercisetracker.database.entities.RoutineWithScheduleAndExerciseProgresses
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOrder
import com.aamo.exercisetracker.utility.extensions.date.weeks
import com.aamo.exercisetracker.utility.extensions.general.EMPTY
import com.aamo.exercisetracker.utility.extensions.general.applyIf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds

@Serializable
data class DailiesScreen(val initialDay: Day)

class DailiesScreenViewModel(
  fetchRoutineData: () -> Flow<List<RoutineWithScheduleAndExerciseProgresses>>,
  fetchTrackedProgresses: () -> Flow<List<TrackedProgress>>
) : ViewModel() {
  private val _routines =
    MutableStateFlow<List<RoutineWithScheduleAndExerciseProgresses>>(emptyList())
  val routines = _routines.asStateFlow()

  private val _trackedProgresses = MutableStateFlow<List<TrackedProgress>>(emptyList())
  val trackedProgresses = _trackedProgresses.asStateFlow()

  private var routinesLoading by mutableStateOf(true)
  private var progressesLoading by mutableStateOf(true)

  val isLoading by derivedStateOf { routinesLoading || progressesLoading }

  init {
    viewModelScope.launch {
      fetchRoutineData().collect {
        _routines.value = it
        routinesLoading = false
      }
    }
    viewModelScope.launch {
      fetchTrackedProgresses().collect {
        _trackedProgresses.value = it
        progressesLoading = false
      }
    }
  }
}

fun NavGraphBuilder.dailiesScreen(
  onSelectRoutine: (Long) -> Unit, onTrackedProgressSelected: (progressId: Long) -> Unit
) {
  composable<DailiesScreen> { stack ->
    val initialDay = stack.toRoute<DailiesScreen>().initialDay
    val routineDao =
      RoutineDatabase.getDatabase(LocalContext.current.applicationContext).routineDao()
    val trackedProgressDao =
      RoutineDatabase.getDatabase(LocalContext.current.applicationContext).trackedProgressDao()

    val viewmodel: DailiesScreenViewModel = viewModel(factory = viewModelFactory {
      initializer {
        DailiesScreenViewModel(fetchRoutineData = {
          routineDao.getRoutinesWithScheduleAndProgressesFlow()
        }, fetchTrackedProgresses = {
          trackedProgressDao.getProgressesWithValuesFlow().map { map ->
            map.filter { item ->
              if (item.key.intervalWeeks == 0) false
              else {
                item.value.maxByOrNull { value -> value.addedDate }?.addedDate?.time?.let { addedMillis -> System.currentTimeMillis() - addedMillis >= item.key.intervalWeeks.weeks.inWholeMilliseconds }
                  ?: true
              }
            }.map { it.key }
          }
        })
      }
    })
    val routines by viewmodel.routines.collectAsStateWithLifecycle()
    val progresses by viewmodel.trackedProgresses.collectAsStateWithLifecycle()

    DailiesScreen(
      routines = routines,
      progresses = progresses,
      isLoading = viewmodel.isLoading,
      initialDay = initialDay,
      onRoutineSelected = onSelectRoutine,
      onTrackedProgressSelected = onTrackedProgressSelected,
      modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    )
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun DailiesScreen(
  routines: List<RoutineWithScheduleAndExerciseProgresses>,
  progresses: List<TrackedProgress>,
  isLoading: Boolean,
  initialDay: Day,
  onRoutineSelected: (routineId: Long) -> Unit,
  onTrackedProgressSelected: (progressId: Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  val days = Calendar.getInstance().getLocalDayOrder()
  val todayIndex = days.indexOf(Day.today())
  val pagerState =
    rememberPagerState(pageCount = { days.size }, initialPage = days.indexOf(initialDay))

  Surface(modifier = modifier) {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxSize()
        .padding(vertical = 8.dp)
    ) {
      HorizontalPager(
        pageSize = PageSize.Fill,
        state = pagerState,
        pageSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 20.dp),
        modifier = Modifier
          .fillMaxHeight()
          .weight(1f)
      ) { pageIndex ->
        val isToday = pageIndex == todayIndex
        val pageDateMillis = remember {
          LocalDate.now().atStartOfDay().plusDays((pageIndex - todayIndex).toLong())
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
        val dayRoutines = remember(routines) {
          routines.filter { it.schedule?.isDaySelected(days[pageIndex].getDayNumber()) == true }
            .map { (routine, _, progresses) ->
              object {
                val routine = routine
                val finishedExerciseCount = progresses.count { (_, progress) ->
                  progress?.finishedDate?.time?.compareTo(pageDateMillis)?.let { it > 0 } == true
                }
                val totalExerciseCount = progresses.size
              }
            }.sortedWith(
              comparator = compareBy(
                { it.finishedExerciseCount == it.totalExerciseCount },
                { it.routine.name },
              )
            )
        }

        val pageOffset =
          ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue

        Surface(
          border = applyIf(isToday) { BorderStroke(2.dp, MaterialTheme.colorScheme.primary) },
          shape = CardDefaults.outlinedShape,
          tonalElevation = 1.dp,
          modifier = Modifier
            .fillMaxSize()
            .padding(vertical = (16 * (pageOffset.coerceIn(0f, 1f))).dp)
            .graphicsLayer {
              alpha = lerp(start = .7f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
            }) {
          Column {
            Text(
              text = stringResource(days[(pageIndex).mod(7)].nameResourceKey),
              style = MaterialTheme.typography.headlineLarge,
              textAlign = TextAlign.Center,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp)
            )
            LoadingScreen(enabled = isLoading, modifier = Modifier.padding(bottom = 56.dp)) {
              LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)
              ) {
                items(dayRoutines, key = { it.routine.id }) { routine ->
                  val isFinished = routine.finishedExerciseCount == routine.totalExerciseCount

                  Button(
                    enabled = pagerState.currentPage == pageIndex,
                    onClick = { onRoutineSelected(routine.routine.id) },
                    shape = RoundedCornerShape(8.dp),
                    colors = if (isFinished) ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.surfaceVariant,
                      contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                      disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                      disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    else ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.secondary,
                      contentColor = MaterialTheme.colorScheme.onSecondary,
                      disabledContainerColor = MaterialTheme.colorScheme.secondary,
                      disabledContentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(
                      horizontalArrangement = Arrangement.SpaceBetween,
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                    ) {
                      Text(text = routine.routine.name, fontWeight = FontWeight.Bold)
                      if (isFinished) {
                        Icon(
                          imageVector = Icons.Filled.Done,
                          contentDescription = stringResource(R.string.cd_done)
                        )
                      }
                      else {
                        Text(
                          text = "${routine.finishedExerciseCount}/${routine.totalExerciseCount}",
                          fontWeight = FontWeight.Normal
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      if (progresses.isNotEmpty()) {
        Surface(
          tonalElevation = 2.dp,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .heightIn(max = 250.dp)
        ) {
          Column {
            Text(
              text = stringResource(R.string.title_scheduled_trackers),
              textAlign = TextAlign.Center,
              style = MaterialTheme.typography.headlineSmall,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
            )
            LazyColumn(modifier = Modifier
              .padding(horizontal = 16.dp)
              .padding(bottom = 8.dp)) {
              items(items = progresses, key = { it.id }) { progress ->
                Button(
                  onClick = { onTrackedProgressSelected(progress.id) },
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                  ),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(text = progress.name, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Suppress("HardCodedStringLiteral")
@Preview(showSystemUi = true)
@Composable
private fun PreviewLight() {
  ExerciseTrackerTheme(darkTheme = false) {
    Scaffold { paddingValues ->
      DailiesScreen(
        initialDay = Day.TUESDAY,
        routines = listOf(
          RoutineWithScheduleAndExerciseProgresses(
            routine = Routine(
              id = 0, name = "Routine 1"
            ), schedule = RoutineSchedule(
              id = 0,
              routineId = 0,
              sunday = true,
              monday = true,
              tuesday = true,
              wednesday = true,
              thursday = true,
              friday = true,
              saturday = true
            ), exerciseProgresses = listOf(
              ExerciseWithProgress(
                exercise = Exercise(
                  id = 0, routineId = 0, name = String.EMPTY, restDuration = 0.seconds
                ), progress = null
              )
            )
          ), RoutineWithScheduleAndExerciseProgresses(
            routine = Routine(
              id = 1, name = "Routine 2"
            ), schedule = RoutineSchedule(
              id = 1,
              routineId = 1,
              sunday = true,
              monday = true,
              tuesday = true,
              wednesday = true,
              thursday = true,
              friday = true,
              saturday = true
            ), exerciseProgresses = emptyList()
          )
        ),
        progresses = listOf(
          TrackedProgress(id = 0, name = "Progress 1"),
          TrackedProgress(id = 1, name = "Progress 2"),
          TrackedProgress(id = 2, name = "Progress 3"),
          TrackedProgress(id = 3, name = "Progress 3"),
          TrackedProgress(id = 4, name = "Progress 3"),
          TrackedProgress(id = 5, name = "Progress 3"),
          TrackedProgress(id = 6, name = "Progress 3"),
          TrackedProgress(id = 7, name = "Progress 3"),
        ),
        isLoading = false,
        onRoutineSelected = {},
        onTrackedProgressSelected = {},
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
      )
    }
  }
}

@Suppress("HardCodedStringLiteral")
@Preview(showSystemUi = true)
@Composable
private fun PreviewDark() {
  ExerciseTrackerTheme(darkTheme = true) {
    Scaffold { paddingValues ->
      DailiesScreen(
        initialDay = Day.TUESDAY,
        routines = listOf(
          RoutineWithScheduleAndExerciseProgresses(
            routine = Routine(
              id = 0, name = "Routine 1"
            ), schedule = RoutineSchedule(
              id = 0,
              routineId = 0,
              sunday = true,
              monday = true,
              tuesday = true,
              wednesday = true,
              thursday = true,
              friday = true,
              saturday = true
            ), exerciseProgresses = listOf(
              ExerciseWithProgress(
                exercise = Exercise(
                  id = 0, routineId = 0, name = String.EMPTY, restDuration = 0.seconds
                ), progress = null
              )
            )
          ), RoutineWithScheduleAndExerciseProgresses(
            routine = Routine(
              id = 1, name = "Routine 2"
            ), schedule = RoutineSchedule(
              id = 1,
              routineId = 1,
              sunday = true,
              monday = true,
              tuesday = true,
              wednesday = true,
              thursday = true,
              friday = true,
              saturday = true
            ), exerciseProgresses = emptyList()
          )
        ),
        progresses = listOf(
          TrackedProgress(id = 0, name = "Progress 1"),
          TrackedProgress(id = 1, name = "Progress 2"),
        ),
        isLoading = false,
        onRoutineSelected = {},
        onTrackedProgressSelected = {},
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
      )
    }
  }
}