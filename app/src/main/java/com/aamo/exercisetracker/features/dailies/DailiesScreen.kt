package com.aamo.exercisetracker.features.dailies

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOfWeek
import com.aamo.exercisetracker.utility.extensions.date.getLocalListOfDays
import com.aamo.exercisetracker.utility.extensions.modifier.applyIf
import kotlinx.serialization.Serializable
import java.util.Calendar
import kotlin.math.absoluteValue

@Serializable data class Dailies(val initialDayNumber: Int = 1)

fun NavGraphBuilder.dailiesDestination() {
  composable<Dailies> { navStack ->
    DailiesScreen(initialPage = (navStack.toRoute() as Dailies).initialDayNumber - 1)
  }
}

@Composable
fun DailiesScreen(initialPage: Int = 0) {
  val today = Calendar.getInstance().getLocalDayOfWeek()
  val days = Calendar.getInstance().getLocalListOfDays()
  val pagerState = rememberPagerState(pageCount = { 7 }, initialPage = initialPage)

  Scaffold { innerPadding ->
    Surface(modifier = Modifier.padding(innerPadding)) {
      HorizontalPager(
        pageSize = PageSize.Fill,
        state = pagerState,
        pageSpacing = 8.dp,
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxHeight()
      ) { pageIndex ->
        val pageOffset =
          ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue

        Card(
          border = BorderStroke(
            2.dp, MaterialTheme.colorScheme.outline
          ).applyIf(today.getDayNumber() - 1 == pageIndex /* IS TODAY */),
          modifier = Modifier
            .fillMaxSize()
            .padding(vertical = (32 + 32 * (pageOffset.coerceIn(0f, 1f))).dp)
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
              .padding(top = 24.dp)
          )
        }
      }
    }
  }
}