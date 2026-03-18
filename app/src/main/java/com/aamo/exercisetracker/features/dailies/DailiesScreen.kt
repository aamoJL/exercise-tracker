package com.aamo.exercisetracker.features.dailies

import android.annotation.SuppressLint
import android.app.UiModeManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.core.content.getSystemService
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
import com.aamo.exercisetracker.features.dailies.components.UnfinishedTrackedProgressesModalBottomSheet
import com.aamo.exercisetracker.features.dailies.models.DailiesRoutineModel
import com.aamo.exercisetracker.features.dailies.use_cases.fetchUnfinishedTrackedProgressesFlow
import com.aamo.exercisetracker.features.dailies.use_cases.fetchWeeklyRoutineScheduleFlow
import com.aamo.exercisetracker.ui.components.BackgroundSurface
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOrder
import com.aamo.exercisetracker.utility.extensions.general.applyIf
import com.aamo.exercisetracker.utility.extensions.general.ifElse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.Calendar
import kotlin.math.absoluteValue

typealias WeeklySchedule = List<List<DailiesRoutineModel>>

@Serializable
data class DailiesScreen(val initialDay: Day)

class DailiesScreenViewModel(
  fetchWeeklySchedule: () -> Flow<WeeklySchedule>,
  fetchTrackedProgresses: () -> Flow<List<TrackedProgress>>
) : ViewModel() {

  val weeklySchedule = fetchWeeklySchedule().stateIn(
    scope = viewModelScope, started = SharingStarted.Lazily, initialValue = null
  )

  val trackedProgresses = fetchTrackedProgresses().stateIn(
    scope = viewModelScope, started = SharingStarted.Lazily, initialValue = null
  )
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
            dao = routineDao,
            weekDays = Calendar.getInstance().getLocalDayOrder(),
            currentDate = LocalDate.now()
          )
        }, fetchTrackedProgresses = {
          fetchUnfinishedTrackedProgressesFlow(
            dao = trackedProgressDao, currentTimeMillis = System.currentTimeMillis()
          )
        })
      }
    })
    val weeklySchedule by viewmodel.weeklySchedule.collectAsStateWithLifecycle()
    val progresses by viewmodel.trackedProgresses.collectAsStateWithLifecycle()

    DailiesScreenContent(
      weeklySchedule = weeklySchedule ?: emptyList(),
      trackedProgresses = progresses ?: emptyList(),
      isLoading = weeklySchedule == null || progresses == null,
      initialDay = initialDay,
      onRoutineSelected = onSelectRoutine,
      onTrackedProgressSelected = onTrackedProgressSelected,
    )
  }
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DailiesScreenContent(
  weeklySchedule: WeeklySchedule,
  trackedProgresses: List<TrackedProgress>,
  isLoading: Boolean,
  initialDay: Day,
  onRoutineSelected: (routineId: Long) -> Unit,
  onTrackedProgressSelected: (progressId: Long) -> Unit,
) {
  val context = LocalContext.current
  val configuration = LocalConfiguration.current
  val days = Calendar.getInstance().getLocalDayOrder()
  val todayIndex = days.indexOf(Day.today())
  val pagerState =
    rememberPagerState(pageCount = { days.size }, initialPage = days.indexOf(initialDay))

  var showTrackedProgressSheet by remember { mutableStateOf(false) }

  BackgroundSurface(modifier = Modifier) {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 8.dp)
    ) {
      CenterAlignedTopAppBar(
        title = { Text(stringResource(R.string.label_dailies)) },
        actions = {
          IconButton(
            onClick = {
              context.getSystemService<UiModeManager>()?.also { manager ->
                manager.setApplicationNightMode(
                  if (configuration.isNightModeActive) UiModeManager.MODE_NIGHT_NO
                  else UiModeManager.MODE_NIGHT_YES
                )
              }
            }) {
            Icon(
              painter = ifElse(
                condition = configuration.isNightModeActive,
                ifTrue = { painterResource(R.drawable.rounded_light_mode_24) },
                ifFalse = { painterResource(R.drawable.dark_mode_24px) }),
              contentDescription = stringResource(R.string.cd_change_app_theme)
            )
          }
        },
      )
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

        @SuppressLint("FrequentlyChangingValue") val pageOffset =
          ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue

        Surface(
          border = applyIf(isToday) { BorderStroke(1.dp, MaterialTheme.colorScheme.primary) },
          shape = CardDefaults.outlinedShape,
          color = MaterialTheme.colorScheme.surfaceContainer,
          modifier = Modifier
            .fillMaxSize()
            .padding(vertical = (16 * (pageOffset.coerceIn(0f, 1f))).dp)
            .graphicsLayer {
              alpha = lerp(start = .7f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
            }) {
          Column {
            Text(
              text = stringResource(days[(pageIndex).mod(7)].nameResourceKey),
              style = MaterialTheme.typography.displaySmall,
              textAlign = TextAlign.Center,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
            )
            LoadingScreen(loading = isLoading, modifier = Modifier.padding(bottom = 56.dp)) {
              LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
              ) {
                items(dayRoutines, key = { it.routine.id }) { routine ->
                  val isFinished = routine.progress.finishedCount == routine.progress.totalCount
                  val unfinishedColor = MaterialTheme.colorScheme.primaryContainer
                  val finishedColor = MaterialTheme.colorScheme.surfaceVariant

                  // Button will be disabled if the page is not active
                  //  so the user does not accidentally press it when changing page
                  OutlinedButton(
                    enabled = pagerState.currentPage == pageIndex,
                    onClick = { onRoutineSelected(routine.routine.id) },
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = if (isFinished) ButtonDefaults.outlinedButtonColors(
                      containerColor = finishedColor,
                      contentColor = MaterialTheme.colorScheme.contentColorFor(finishedColor),
                      disabledContainerColor = finishedColor,
                      disabledContentColor = MaterialTheme.colorScheme.contentColorFor(
                        finishedColor
                      )
                    )
                    else ButtonDefaults.outlinedButtonColors(
                      containerColor = unfinishedColor,
                      contentColor = MaterialTheme.colorScheme.contentColorFor(unfinishedColor),
                      disabledContainerColor = unfinishedColor,
                      disabledContentColor = MaterialTheme.colorScheme.contentColorFor(
                        unfinishedColor
                      )
                    ),
                    border = if (isFinished) BorderStroke(
                      1.dp, MaterialTheme.colorScheme.outlineVariant
                    )
                    else BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .25f)),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
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
                        Box(
                          contentAlignment = Alignment.Center, modifier = Modifier.height(24.dp)
                        ) {
                          Text(
                            text = "${routine.progress.finishedCount}/${routine.progress.totalCount}",
                            fontWeight = FontWeight.Normal,
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }

          if (isToday && trackedProgresses.isNotEmpty()) {
            Box(contentAlignment = Alignment.BottomEnd) {
              FloatingActionButton(
                onClick = { showTrackedProgressSheet = true },
                containerColor = ButtonDefaults.buttonColors().containerColor,
                modifier = Modifier
                  .padding(16.dp)
                  .minimumInteractiveComponentSize()
              ) {
                BadgedBox(
                  badge = {
                    Badge(
                      containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                      contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ) { Text(trackedProgresses.size.toString()) }
                  }) {
                  Icon(
                    painter = painterResource(R.drawable.rounded_bar_chart_24),
                    contentDescription = stringResource(R.string.cd_show_unfinished_tracked_progresses)
                  )
                }
              }
            }
          }
        }
      }
    }
    UnfinishedTrackedProgressesModalBottomSheet(
      trackedProgresses = trackedProgresses,
      show = showTrackedProgressSheet,
      onDismissRequest = { showTrackedProgressSheet = false },
      onTrackedProgressSelected = { onTrackedProgressSelected(it) },
    )
  }
}

@Suppress("HardCodedStringLiteral")
@PreviewLightDark
@Composable
private fun Preview() {
  ExerciseTrackerTheme {
    DailiesScreenContent(
      initialDay = Day.MONDAY,
      weeklySchedule = listOf(
        listOf(
          DailiesRoutineModel(
            routine = Routine(id = 3, name = "Routine 3"), progress = DailiesRoutineModel.Progress(
              finishedCount = 0, totalCount = 2
            )
          ), DailiesRoutineModel(
            routine = Routine(id = 1, name = "Routine 1"), progress = DailiesRoutineModel.Progress(
              finishedCount = 2, totalCount = 2
            )
          ), DailiesRoutineModel(
            routine = Routine(id = 2, name = "Routine 2"), progress = DailiesRoutineModel.Progress(
              finishedCount = 1, totalCount = 2
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
    )
  }
}