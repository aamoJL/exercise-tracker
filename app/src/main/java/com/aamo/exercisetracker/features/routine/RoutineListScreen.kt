package com.aamo.exercisetracker.features.routine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aamo.exercisetracker.ui.components.SearchTextField
import com.aamo.exercisetracker.utility.extensions.string.EMPTY
import kotlinx.serialization.Serializable

@Serializable object RoutineListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineListScreen(onSelectRoutine: (id: Int) -> Unit, onAdd: () -> Unit) {
  Surface {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .imePadding()
    ) {
      TopAppBar(title = { Text(String.EMPTY) }, actions = {
        SearchTextField(
          value = String.EMPTY,
          onValueChange = { /* TODO: value change command */ },
          modifier = Modifier.height(50.dp)
        )
        IconButton(onClick = onAdd) {
          Icon(imageVector = Icons.Filled.Add, contentDescription = "Add routine")
        }
      })
      LazyColumn(
        userScrollEnabled = true,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
          .padding(top = 4.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
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