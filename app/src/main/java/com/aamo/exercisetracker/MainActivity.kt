package com.aamo.exercisetracker

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import com.aamo.exercisetracker.features.home.HomePage
import com.aamo.exercisetracker.services.CountDownTimerService
import com.aamo.exercisetracker.services.StopwatchTimerService
import com.aamo.exercisetracker.services.TimerServiceProperties
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

    TimerServiceProperties.createNotificationChannel(context = applicationContext, activity = this)
    // Timer services must be started here because the onTaskRemoved does not get
    //  invoked on binding
    startService(Intent(this, CountDownTimerService::class.java))
    startService(Intent(this, StopwatchTimerService::class.java))
  }

  override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<out String?>, grantResults: IntArray, deviceId: Int
  ) {
    if (requestCode == TimerServiceProperties.PERMISSION_CODE) {
      if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
        TimerServiceProperties.createNotificationChannel(
          context = applicationContext, activity = this
        )
      }
    }

    super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
  }
}