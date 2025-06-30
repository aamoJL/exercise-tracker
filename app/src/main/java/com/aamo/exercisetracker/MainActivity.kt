package com.aamo.exercisetracker

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import com.aamo.exercisetracker.features.home.HomePage
import com.aamo.exercisetracker.services.RestTimerService
import com.aamo.exercisetracker.ui.theme.ExerciseTrackerTheme

class MainActivity : ComponentActivity() {
  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ExerciseTrackerTheme {
        HomePage()
      }
    }

    RestTimerService.createNotificationChannel(context = applicationContext, activity = this)
  }

  override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<out String?>, grantResults: IntArray, deviceId: Int
  ) {
    if (requestCode == RestTimerService.PERMISSION_CODE) {
      if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
        RestTimerService.createNotificationChannel(context = applicationContext, activity = this)
      }
    }

    super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
  }
}