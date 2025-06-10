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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.aamo.exercisetracker.utility.extensions.date.Day
import com.aamo.exercisetracker.utility.extensions.date.getLocalDayOrder
import com.aamo.exercisetracker.utility.extensions.modifier.applyIf
import kotlinx.serialization.Serializable
import java.util.Calendar
import kotlin.math.absoluteValue

@Serializable data class DailiesScreen(val initialDayNumber: Int = 1)

@Composable
fun DailiesScreen(modifier: Modifier = Modifier, initialPage: Int = 0) {
  val today = Day.getByDayNumber(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
  val days = Calendar.getInstance().getLocalDayOrder()
  val pagerState = rememberPagerState(pageCount = { 7 }, initialPage = initialPage)

  Surface(modifier = modifier) {
    HorizontalPager(
      pageSize = PageSize.Fill,
      state = pagerState,
      pageSpacing = 8.dp,
      contentPadding = PaddingValues(horizontal = 24.dp),
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
            .padding(top = 24.dp)
        )
      }
    }
  }
}