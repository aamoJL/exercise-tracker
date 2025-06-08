package com.aamo.exercisetracker.features.routine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.SearchTextField
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import kotlinx.serialization.Serializable

@Serializable object RoutineListScreen

fun NavGraphBuilder.routinesListScreen(
  onBack: () -> Unit, onAddRoutine: (id: Int) -> Unit, onSelectRoutine: (id: Int) -> Unit
) {
  composable<RoutineListScreen> {
    RoutineListScreen(
      onBack = onBack, onAddRoutine = onAddRoutine, onSelectRoutine = onSelectRoutine
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineListScreen(
  onBack: () -> Unit, onAddRoutine: (id: Int) -> Unit, onSelectRoutine: (id: Int) -> Unit
) {
  Scaffold(topBar = {
    TopAppBar(title = { Text("Weekly Routines") }, navigationIcon = {
      BackNavigationIconButton(onBack = onBack)
    }, actions = {
      IconButton(onClick = { onAddRoutine(0) }) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add routine")
      }
    })
  }, modifier = Modifier.imePadding()) { innerPadding ->
    Surface {
      Column(
        modifier = Modifier
          .padding(innerPadding)
          .fillMaxSize()
      ) {
        SearchTextField(
          value = String.EMPTY,
          onValueChange = {/* TODO: value change command */ },
          onClear = { /* TODO: clear command */ },
          modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        )
        LazyColumn(
          userScrollEnabled = true,
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
        ) {
          items(100) { item ->
            ListItem(
              colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
              headlineContent = {
                Text(text = "Routine Headline $item", fontWeight = FontWeight.Bold)
              },
              supportingContent = {
                Text("Supporting")
              },
              leadingContent = {
                Text("Leading")
              },
              overlineContent = {
                Text("Overline")
              },
              trailingContent = {
                Text("Trailing")
              },
              modifier = Modifier.clickable { onSelectRoutine(item) })
          }
        }
      }
    }
  }
}