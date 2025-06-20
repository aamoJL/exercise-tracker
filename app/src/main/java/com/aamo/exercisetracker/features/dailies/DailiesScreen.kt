package com.aamo.exercisetracker.features.dailies

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aamo.exercisetracker.database.RoutineDatabase
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOrder
import com.aamo.exercisetracker.utility.extensions.general.applyIf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import java.util.Calendar
import kotlin.math.absoluteValue

@Serializable
data class DailiesScreen(val initialDay: Day)

@Composable
fun DailiesScreen(
  initialDay: Day, onRoutineSelected: (routineId: Long) -> Unit, modifier: Modifier = Modifier
) {
  val today = Day.today()
  val days = Calendar.getInstance().getLocalDayOrder()
  val pagerState = rememberPagerState(pageCount = { 7 }, initialPage = days.indexOf(initialDay))

  val routines by RoutineDatabase.getDatabase(LocalContext.current.applicationContext).routineDao()
    .getRoutinesWithScheduleFlow().map { it.filter { it.schedule != null } }
    .collectAsStateWithLifecycle(initialValue = emptyList())

  Surface(modifier = modifier) {
    HorizontalPager(
      pageSize = PageSize.Fill,
      state = pagerState,
      pageSpacing = 8.dp,
      contentPadding = PaddingValues(horizontal = 24.dp),
      modifier = Modifier.fillMaxHeight()
    ) { pageIndex ->
      val isToday = days[pageIndex] == today
      val pageOffset =
        ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue
      val dayRoutines = routines.filter {
        it.schedule?.isDaySelected(
          days.elementAt(pageIndex).getDayNumber()
        ) == true
      }.map { it.routine }

      Card(
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        border = applyIf(isToday, BorderStroke(2.dp, MaterialTheme.colorScheme.outline)),
        modifier = Modifier
          .fillMaxSize()
          .padding(vertical = (16 + 32 * (pageOffset.coerceIn(0f, 1f))).dp)
          .graphicsLayer {
            alpha = lerp(
              start = 0.5f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f)
            )
          }) {
        Text(
          text = stringResource(days[(pageIndex).mod(7)].nameResourceKey),
          style = MaterialTheme.typography.headlineLarge,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp)
        )
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(8.dp)
        ) {
          items(dayRoutines) { routine ->
            OutlinedCard(
              enabled = pagerState.currentPage == pageIndex,
              onClick = { onRoutineSelected(routine.id) },
              border = CardDefaults.outlinedCardBorder(enabled = true),
              colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = routine.name,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 28.dp)
              )
            }
          }
        }
      }
    }
  }
}