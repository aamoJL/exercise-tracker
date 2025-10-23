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
import androidx.compose.ui.res.painterResource
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
import com.aamo.exercisetracker.database.entities.Routine
import com.aamo.exercisetracker.database.entities.TrackedProgress
import com.aamo.exercisetracker.features.dailies.DailiesScreenViewModel.RoutineModel
import com.aamo.exercisetracker.features.dailies.use_cases.fetchUnfinishedTrackedProgressesFlow
import com.aamo.exercisetracker.features.dailies.use_cases.fetchWeeklyRoutineScheduleFlow
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOrder
import com.aamo.exercisetracker.utility.extensions.general.applyIf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.Calendar
import kotlin.math.absoluteValue

typealias WeeklySchedule = List<List<RoutineModel>>

@Serializable
data class DailiesScreen(val initialDay: Day)

class DailiesScreenViewModel(
  fetchWeeklySchedule: () -> Flow<WeeklySchedule>,
  fetchTrackedProgresses: () -> Flow<List<TrackedProgress>>
) : ViewModel() {
  data class RoutineModel(
    val routine: Routine, val progress: Progress
  ) {
    data class Progress(val finishedCount: Int, val totalCount: Int)
  }

  private val _weeklySchedule = MutableStateFlow<WeeklySchedule>(emptyList())
  val weeklySchedule = _weeklySchedule.asStateFlow()

  private val _trackedProgresses = MutableStateFlow<List<TrackedProgress>>(emptyList())
  val trackedProgresses = _trackedProgresses.asStateFlow()

  private var weeklyScheduleLoading by mutableStateOf(true)
  private var progressesLoading by mutableStateOf(true)

  val isLoading by derivedStateOf { weeklyScheduleLoading || progressesLoading }

  init {
    viewModelScope.launch {
      fetchWeeklySchedule().collect {
        _weeklySchedule.value = it
        weeklyScheduleLoading = false
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
        DailiesScreenViewModel(fetchWeeklySchedule = {
          fetchWeeklyRoutineScheduleFlow(
            weekDays = Calendar.getInstance().getLocalDayOrder(), currentDate = LocalDate.now()
          ) {
            routineDao.getRoutineScheduleWithProgressFlow()
          }
        }, fetchTrackedProgresses = {
          fetchUnfinishedTrackedProgressesFlow(currentTimeMillis = System.currentTimeMillis()) {
            trackedProgressDao.getProgressesWithValuesFlow()
          }
        })
      }
    })
    val weeklySchedule by viewmodel.weeklySchedule.collectAsStateWithLifecycle()
    val progresses by viewmodel.trackedProgresses.collectAsStateWithLifecycle()

    DailiesScreen(
      weeklySchedule = weeklySchedule,
      trackedProgresses = progresses,
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
  weeklySchedule: WeeklySchedule,
  trackedProgresses: List<TrackedProgress>,
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

        val dayRoutines = remember(weeklySchedule) {
          weeklySchedule.elementAtOrNull(pageIndex)?.sortedWith(
            comparator = compareBy(
              { it.progress.finishedCount == it.progress.totalCount },
              { it.routine.name },
            )
          )
        } ?: emptyList()

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
                  val isFinished = routine.progress.finishedCount == routine.progress.totalCount

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
                          painter = painterResource(R.drawable.rounded_done_all_24),
                          contentDescription = stringResource(R.string.cd_done)
                        )
                      }
                      else {
                        Text(
                          text = "${routine.progress.finishedCount}/${routine.progress.totalCount}",
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
      if (trackedProgresses.isNotEmpty()) {
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
            LazyColumn(
              modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
            ) {
              items(items = trackedProgresses, key = { it.id }) { progress ->
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
private fun Preview() {
  ExerciseTrackerTheme(darkTheme = true) {
    Scaffold { paddingValues ->
      DailiesScreen(
        initialDay = Day.MONDAY,
        weeklySchedule = listOf(
          listOf(
            RoutineModel(
              routine = Routine(id = 0, name = "Routine 1"), progress = RoutineModel.Progress(
                finishedCount = 2, totalCount = 2
              )
            )
          )
        ),
        trackedProgresses = listOf(
          TrackedProgress(id = 0, name = "Progress 1"),
          TrackedProgress(id = 1, name = "Progress 2"),
          TrackedProgress(id = 2, name = "Progress 3"),
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