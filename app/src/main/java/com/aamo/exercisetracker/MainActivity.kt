package com.aamo.exercisetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.aamo.exercisetracker.utility.extensions.string.EMPTY

/* Enum class for screen navigation
 */
enum class Screen(private val route: String, val argumentName: String = "") {
  Home("Home");

  fun getRoute(): String = when (argumentName) {
    String.EMPTY -> route
    else -> route.plus("{$argumentName}")
  }
}

class MainActivity : ComponentActivity() {
  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ExerciseTrackerTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          NavHost(
            navController = rememberNavController(),
            startDestination = Screen.Home.getRoute()
          ) {
            composable(route = Screen.Home.getRoute()) {
              Scaffold { innerPadding ->
                Surface(modifier = Modifier.padding(innerPadding)) {
                  Column {
                    Box(
                      contentAlignment = Alignment.Center,
                      modifier = Modifier.fillMaxWidth().weight(2f)
                    ){
                      Text(
                        text = "Home",
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                    }
                    Box(
                      modifier = Modifier.fillMaxWidth().weight(5f),
                      contentAlignment = Alignment.Center
                    ) {
                      Column(
                        modifier = Modifier.width(IntrinsicSize.Max),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                      ) {
                        Button(
                          modifier = Modifier.fillMaxWidth(),
                          onClick = {}) {
                          Text("Today's exercises")
                        }
                        Button(
                          modifier = Modifier.fillMaxWidth(),
                          onClick = {}) {
                          Text("Weekly routines")
                        }
                        Button(
                          modifier = Modifier.fillMaxWidth(),
                          onClick = {}) {
                          Text("Monthly progress")
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}