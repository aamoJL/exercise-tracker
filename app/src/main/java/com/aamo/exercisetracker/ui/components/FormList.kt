package com.aamo.exercisetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aamo.exercisetracker.R

@Composable
fun FormList(
  title: String, onAdd: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit
) {
  ElevatedCard(modifier = modifier) {
    ListTitleBar(title = title, onAddClick = onAdd)
    content()
  }
}

@Composable
fun ListTitleBar(title: String, onAddClick: () -> Unit, modifier: Modifier = Modifier) {
  Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = modifier.fillMaxWidth()) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier
        .padding(vertical = 8.dp, horizontal = 16.dp)
        .fillMaxWidth()
    ) {
      Text(
        text = title, style = MaterialTheme.typography.titleLarge
      )
      OutlinedIconButton(
        onClick = { onAddClick() }) {
        Icon(
          painter = painterResource(R.drawable.rounded_add_24),
          contentDescription = stringResource(R.string.cd_add_new_item),
          tint = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}