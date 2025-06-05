package com.aamo.exercisetracker.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.aamo.exercisetracker.R
import kotlinx.serialization.Serializable

@Serializable object Home

fun NavGraphBuilder.homeDestination(
  onTodayClick: () -> Unit,
  onWeeklyClick: () -> Unit,
  onMonthlyClick: () -> Unit,
) {
  composable<Home> {
    HomeScreen(
      onTodayClick = onTodayClick, onWeeklyClick = onWeeklyClick, onMonthlyClick = onMonthlyClick
    )
  }
}

@Composable
fun HomeScreen(
  onTodayClick: () -> Unit = {},
  onWeeklyClick: () -> Unit = {},
  onMonthlyClick: () -> Unit = {},
) {
  Scaffold { innerPadding ->
    Surface(modifier = Modifier.padding(innerPadding)) {
      Column {
        Box(
          contentAlignment = Alignment.Center, modifier = Modifier
            .fillMaxWidth()
            .weight(2f)
        ) {
          Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(5f), contentAlignment = Alignment.Center
        ) {
          Column(
            modifier = Modifier.width(IntrinsicSize.Max),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Button(
              modifier = Modifier.fillMaxWidth(), onClick = onTodayClick
            ) {
              Text("Today's exercises")
            }
            Button(
              modifier = Modifier.fillMaxWidth(), onClick = onWeeklyClick
            ) {
              Text("Weekly routines")
            }
            Button(
              modifier = Modifier.fillMaxWidth(), onClick = onMonthlyClick
            ) {
              Text("Monthly progress")
            }
          }
        }
      }
    }
  }
}