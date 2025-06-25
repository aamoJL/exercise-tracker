package com.aamo.exercisetracker.features.dailies

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.aamo.exercisetracker.database.RoutineDatabase
import com.aamo.exercisetracker.database.entities.RoutineWithSchedule
import com.aamo.exercisetracker.ui.components.LoadingScreen
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOrder
import com.aamo.exercisetracker.utility.extensions.general.applyIf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Calendar
import kotlin.math.absoluteValue

@Serializable
data class DailiesScreen(val initialDay: Day)

class DailiesScreenViewModel(context: Context) : ViewModel() {
  private val database: RoutineDatabase = RoutineDatabase.getDatabase(context)

  private val _routines = MutableStateFlow<List<RoutineWithSchedule>>(emptyList())
  val routines = _routines.asStateFlow()

  var isLoading by mutableStateOf(true)
    private set

  init {
    viewModelScope.launch {
      database.routineDao().getRoutinesWithScheduleFlow().collect {
        _routines.value = it
        isLoading = false
      }
    }
  }
}

fun NavGraphBuilder.dailiesScreen(onRoutineSelected: (Long) -> Unit) {
  composable<DailiesScreen> { stack ->
    val context = LocalContext.current.applicationContext
    val viewmodel: DailiesScreenViewModel = viewModel(factory = viewModelFactory {
      initializer { DailiesScreenViewModel(context = context) }
    })
    val routines by viewmodel.routines.collectAsStateWithLifecycle()
    val initialDay = stack.toRoute<DailiesScreen>().initialDay

    DailiesScreen(
      routines = routines,
      isLoading = viewmodel.isLoading,
      initialDay = initialDay,
      onRoutineSelected = onRoutineSelected,
      modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    )
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun DailiesScreen(
  routines: List<RoutineWithSchedule>,
  isLoading: Boolean,
  initialDay: Day,
  onRoutineSelected: (routineId: Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  val today = Day.today()
  val days = Calendar.getInstance().getLocalDayOrder()
  val pagerState =
    rememberPagerState(pageCount = { days.size }, initialPage = days.indexOf(initialDay))

  Surface(modifier = modifier) {
    HorizontalPager(
      pageSize = PageSize.Fill,
      state = pagerState,
      pageSpacing = 8.dp,
      contentPadding = PaddingValues(horizontal = 24.dp),
      modifier = Modifier.fillMaxHeight()
    ) { pageIndex ->
      val isToday = days[pageIndex] == today
      val dayRoutines = remember(routines) {
        routines.filter { it.schedule?.isDaySelected(days[pageIndex].getDayNumber()) == true }
          .map { it.routine }
      }

      val pageOffset =
        ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue

      Card(
        border = applyIf(isToday, BorderStroke(2.dp, MaterialTheme.colorScheme.primary)),
        modifier = Modifier
          .fillMaxSize()
          .padding(vertical = (16 + 32 * (pageOffset.coerceIn(0f, 1f))).dp)
          .graphicsLayer {
            alpha = lerp(start = 0.5f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
          }) {
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
            items(dayRoutines) { routine ->
              ElevatedCard(
                enabled = pagerState.currentPage == pageIndex,
                onClick = { onRoutineSelected(routine.id) },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.outlinedCardColors(
                  containerColor = MaterialTheme.colorScheme.secondary,
                  disabledContainerColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = routine.name,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}