package com.aamo.exercisetracker.services

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

@Suppress("HardCodedStringLiteral")
class TimerServiceProperties() {
  companion object {
    const val PERMISSION_CODE: Int = 1
    const val CHANNEL_ID: String = "Timer channel"
    const val NOTIFICATION_ID: Int = 0

    fun createNotificationChannel(context: Context, activity: Activity) {
      with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
          ) == PackageManager.PERMISSION_DENIED) {

          ActivityCompat.requestPermissions(
            activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), PERMISSION_CODE
          )

          return@with
        }
        else {
          val name = CHANNEL_ID
          val importance = NotificationManager.IMPORTANCE_LOW
          val channel = NotificationChannel(
            CHANNEL_ID, name, importance
          ).apply {
            description = "Countdown timer notifications"
          }

          val notificationManager = ContextCompat.getSystemService(
            context, NotificationManager::class.java
          ) as NotificationManager

          notificationManager.createNotificationChannel(channel)
        }
      }
    }
  }
}