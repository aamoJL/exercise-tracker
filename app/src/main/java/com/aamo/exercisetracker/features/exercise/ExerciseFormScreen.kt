package com.aamo.exercisetracker.features.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import com.aamo.exercisetracker.ui.components.FormList
import com.aamo.exercisetracker.ui.components.borderlessTextFieldColors
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import kotlinx.serialization.Serializable

@Serializable data class ExerciseFormScreen(val id: Int)
private data class Set(val key: Int, val value: String)

fun NavGraphBuilder.exerciseFormScreen(onBack: () -> Unit, onSave: () -> Unit) {
  composable<ExerciseFormScreen> { navStack ->
    val args: ExerciseFormScreen = navStack.toRoute()
    ExerciseFormScreen(
      id = args.id, onBack = onBack, onSave = onSave
    )
  }
}

fun NavController.toExerciseFormScreen(id: Int) {
  this.navigate(ExerciseFormScreen(id))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseFormScreen(id: Int, onBack: () -> Unit, onSave: () -> Unit) {
  var exerciseName by remember { mutableStateOf(String.EMPTY) }
  var repUnit by remember { mutableStateOf("reps") }
  var sets = remember { mutableStateListOf<Set>(Set(key = 0, value = String.EMPTY)) }
  var nextSetKey by remember { mutableIntStateOf(sets.count()) }

  Scaffold(topBar = {
    TopAppBar(
      title = { Text(text = if (id == 0) "New Exercise" else "Edit Exercise") },
      navigationIcon = { BackNavigationIconButton(onBack = onBack) },
      actions = {
        IconButton(onClick = { onSave }) {
          Icon(imageVector = Icons.Filled.Check, contentDescription = "Save")
        }
      })
  }) { innerPadding ->
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .padding(innerPadding)
        .padding(8.dp)
    ) {
      TextField(
        value = exerciseName,
        label = { Text("Name") },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        onValueChange = { exerciseName = it },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth()
      )
      TextField(
        value = repUnit,
        label = { Text("Set unit") },
        shape = RectangleShape,
        colors = borderlessTextFieldColors(),
        onValueChange = { repUnit = it },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      FormList(
        title = "Sets", onAdd = {
          sets.add(Set(key = nextSetKey, value = String.EMPTY))
          nextSetKey++
        }, modifier = Modifier.fillMaxWidth()
      ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp)) {
          itemsIndexed(items = sets, key = { _, set -> set.key }) { i, set ->
            val dens = LocalDensity.current
            val dismissState = rememberSwipeToDismissBoxState(
              positionalThreshold = { with(dens) { 70.dp.toPx() } })
            val deleteDirection = SwipeToDismissBoxValue.StartToEnd

            LaunchedEffect(dismissState.currentValue) {
              when (dismissState.currentValue) {
                deleteDirection -> {
                  sets.removeAt(i)
                }

                else -> {}
              }
            }
            
            SwipeToDismissBox(
              state = dismissState,
              enableDismissFromStartToEnd = true,
              enableDismissFromEndToStart = false,
              backgroundContent = {
                DismissBoxBackgroundContent(state = dismissState, deleteDirection = deleteDirection)
              },
              modifier = Modifier.animateItem()
            ) {
              TextField(
                value = set.value,
                shape = RectangleShape,
                colors = TextFieldDefaults.colors(
                  unfocusedIndicatorColor = Color.Transparent,
                  focusedIndicatorColor = Color.Transparent,
                  unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline,
                  focusedPlaceholderColor = MaterialTheme.colorScheme.outline
                ),
                placeholder = { Text("amount...") },
                onValueChange = {
                  if (it.isDigitsOnly()) {
                    sets[i] = sets[i].copy(value = it)
                  }
                },
                suffix = { Text(repUnit) },
                keyboardOptions = KeyboardOptions(
                  keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DismissBoxBackgroundContent(
  state: SwipeToDismissBoxState, deleteDirection: SwipeToDismissBoxValue
) {
  val color = when (state.targetValue) {
    deleteDirection -> MaterialTheme.colorScheme.errorContainer
    else -> Color.Transparent
  }
  val icon = when (state.dismissDirection) {
    deleteDirection -> Icons.Filled.Delete
    else -> null
  }
  val alignment = when (deleteDirection) {
    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
    else -> Alignment.CenterStart
  }

  Box(
    contentAlignment = alignment, modifier = Modifier
      .fillMaxSize()
      .background(color)
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.minimumInteractiveComponentSize()
      )
    }
  }
}