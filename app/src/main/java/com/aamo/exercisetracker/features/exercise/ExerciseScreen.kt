package com.aamo.exercisetracker.features.exercise

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.aamo.exercisetracker.ui.components.BackNavigationIconButton
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable data class ExerciseScreen(val id: Int = 0)

fun NavGraphBuilder.exerciseScreen(onBack: () -> Unit, onEdit: (id: Int) -> Unit) {
  composable<ExerciseScreen> { navStack ->
    val id: Int = navStack.toRoute<ExerciseScreen>().id

    ExerciseScreen(onBack = onBack, onEdit = { onEdit(id) })
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ExerciseScreen(onBack: () -> Unit, onEdit: () -> Unit) {
  val sheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true,
    confirmValueChange = { false /* Prevents closing by pressing outside the sheet */ })
  val scope = rememberCoroutineScope()
  var showBottomSheet by remember { mutableStateOf(false) }
  var restProgress by remember { mutableFloatStateOf(0f) } /* TODO: Timer progress */
  var exerciseProgress by remember { mutableFloatStateOf(0f) } /* TODO: Exercise progress */

  Scaffold(
    topBar = {
      TopAppBar(title = { Text("Exercise") }, actions = {
        IconButton(onClick = onEdit) {
          Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit exercise")
        }
      }, navigationIcon = { BackNavigationIconButton(onBack = onBack) })
    }) { innerPadding ->
    Surface(
      modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize()
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.padding(16.dp)
      ) {
        Box(
          contentAlignment = Alignment.Center, modifier = Modifier
            .fillMaxWidth(.7f)
            .aspectRatio(1f)
        ) {
          /* TODO: Segmented progress indicator */
          CircularProgressIndicator(
            progress = { exerciseProgress },
            strokeWidth = 20.dp,
            gapSize = 0.dp,
            strokeCap = StrokeCap.Butt,
            modifier = Modifier.fillMaxSize()
          )
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Sets", style = MaterialTheme.typography.titleMedium)
            Text(
              text = "0/5",
              style = MaterialTheme.typography.displayLarge,
              textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(with(LocalDensity.current) {
              // Centers the text inside the progress indicator
              MaterialTheme.typography.titleMedium.lineHeight.toDp()
            }))
          }
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Card {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
            ) {
              Text("Current set", style = MaterialTheme.typography.titleMedium)
              Text("50 reps", style = MaterialTheme.typography.displayMedium)
            }
          }
          Button(
            shape = CardDefaults.shape, onClick = {
              scope.launch { sheetState.show() }.also {
                showBottomSheet = true
              }
            }, modifier = Modifier
              .heightIn(max = 100.dp)
              .fillMaxSize()
          ) {
            Text("Finished", style = MaterialTheme.typography.titleLarge)
          }
        }
      }
    }

    if (showBottomSheet) {
      ModalBottomSheet(
        sheetState = sheetState, onDismissRequest = { showBottomSheet = false }, dragHandle = null
      ) {
        Column(
          verticalArrangement = Arrangement.Top,
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(.8f)
            .pointerInput(Unit) {
              detectVerticalDragGestures(onVerticalDrag = { change, _ -> change.consume() })
              detectTapGestures(
                onPress = {

                })
            }) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .fillMaxHeight(.8f)
              .fillMaxWidth(.7f)
              .aspectRatio(1f)
          ) {
            CircularProgressIndicator(
              progress = { restProgress },
              strokeWidth = 20.dp,
              gapSize = 0.dp,
              strokeCap = StrokeCap.Butt,
              modifier = Modifier.fillMaxSize()
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Rest", style = MaterialTheme.typography.titleMedium)
              Text(
                text = "00:00",
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center
              )
              Spacer(Modifier.height(with(LocalDensity.current) {
                // Centers the text inside the progress indicator
                MaterialTheme.typography.titleMedium.lineHeight.toDp()
              }))
            }
          }
          Button(onClick = {
            scope.launch { sheetState.hide() }.invokeOnCompletion { showBottomSheet = false }
          }) {
            Text("Cancel")
          }
        }
      }
    }
  }
}