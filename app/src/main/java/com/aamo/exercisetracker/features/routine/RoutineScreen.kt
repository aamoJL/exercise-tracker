package com.aamo.exercisetracker.features.routine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import kotlinx.serialization.Serializable

@Serializable data class RoutineScreen(val id: Int = 0)

fun NavGraphBuilder.routineScreen(
  onBack: () -> Unit,
  onAddExercise: () -> Unit,
  onSelectExercise: (Int) -> Unit,
  onEdit: (id: Int) -> Unit
) {
  composable<RoutineScreen> { navStack ->
    val id: Int = navStack.toRoute<RoutineScreen>().id

    RoutineScreen(
      onBack = onBack,
      onAddExercise = onAddExercise,
      onSelectExercise = onSelectExercise,
      onEdit = { onEdit(id) })
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RoutineScreen(
  onBack: () -> Unit,
  onAddExercise: () -> Unit,
  onSelectExercise: (id: Int) -> Unit,
  onEdit: () -> Unit
) {
  Scaffold(topBar = {
    TopAppBar(title = { Text("Routine") }, navigationIcon = {
      BackNavigationIconButton(onBack = onBack)
    }, actions = {
      IconButton(onClick = onEdit) {
        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit routine")
      }
      IconButton(onClick = onAddExercise) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add exercise")
      }
    })
  }) { innerPadding ->
    Surface(modifier = Modifier.padding(innerPadding)) {
      LazyColumn(
        userScrollEnabled = true,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
          .padding(8.dp)
          .clip(RoundedCornerShape(8.dp))
      ) {
        items(5) { item ->
          ListItem(
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            headlineContent = {
              Text(text = "Exercise Headline $item", fontWeight = FontWeight.Bold)
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
            modifier = Modifier.clickable { onSelectExercise(item) })
        }
      }
    }
  }
}