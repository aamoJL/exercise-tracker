package com.aamo.exercisetracker.services

import android.Manifest
import android.R.drawable.ic_lock_idle_alarm
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aamo.exercisetracker.utility.extensions.general.onFalse
import com.aamo.exercisetracker.utility.tags.DebugTag

interface IStopwatchTimerService {
  fun start(startTime: Long) {}
  fun cancel() {}
}

class StopwatchTimerService() : Service(), IStopwatchTimerService {
  private data class Properties(val startTime: Long)

  private val binder = BinderHelper()
  private var properties: Properties? = null

  override fun onBind(p0: Intent?): IBinder {
    return binder
  }

  override fun onTaskRemoved(rootIntent: Intent?) {
    super.onTaskRemoved(rootIntent)
    // Stop this service when the activity has been closed
    //  works only if this service has been started with startService once
    cancel()
    stopSelf()
  }

  override fun start(startTime: Long) {
    cancel()
    properties = Properties(startTime = startTime)
  }

  override fun cancel() {
    properties = null
    hideNotification()
  }

  fun showNotification(title: String) {
    properties?.also {
      sendNotification(title = title, startTime = it.startTime)
    }
  }

  fun hideNotification() {
    with(NotificationManagerCompat.from(this)) {
      cancel(TimerServiceProperties.NOTIFICATION_ID)
    }
  }

  private fun sendNotification(title: String, startTime: Long) {
    val notificationBuilder =
      NotificationCompat.Builder(this, TimerServiceProperties.CHANNEL_ID).setContentTitle(title)
        .setSmallIcon(ic_lock_idle_alarm).setPriority(NotificationCompat.PRIORITY_LOW)
        .setOnlyAlertOnce(true).setUsesChronometer(true).setChronometerCountDown(false)
        .setWhen(startTime).setOngoing(true)

    with(NotificationManagerCompat.from(this)) {
      if (checkPermission()) {
        notify(TimerServiceProperties.NOTIFICATION_ID, notificationBuilder.build())
      }
    }
  }

  @Suppress("HardCodedStringLiteral")
  private fun checkPermission(): Boolean {
    return (ActivityCompat.checkSelfPermission(
      this, Manifest.permission.POST_NOTIFICATIONS
    ) != PackageManager.PERMISSION_DENIED).onFalse {
      Log.e(DebugTag.ERROR.name, "Permission denied")
    }
  }

  inner class BinderHelper : Binder() {
    fun getService(): StopwatchTimerService = this@StopwatchTimerService
  }
}