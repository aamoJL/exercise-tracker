package com.aamo.exercisetracker.services

import android.Manifest
import android.R.drawable.ic_lock_idle_alarm
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aamo.exercisetracker.utility.extensions.general.onFalse
import com.aamo.exercisetracker.utility.extensions.general.onTrue
import com.aamo.exercisetracker.utility.tags.DebugTag
import kotlin.time.Duration

interface ICountdownTimerService {
  fun start(duration: Duration) {}
  fun finish() {}
  fun cancel() {}
}

open class CountdownTimerService() : Service(), ICountdownTimerService {
  private data class TimerProperties(
    val startTime: Long,
    val duration: Duration,
  )

  private val binder = BinderHelper()
  private var properties: TimerProperties? = null

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

  override fun start(duration: Duration) {
    cancel()
    properties = TimerProperties(startTime = System.currentTimeMillis(), duration = duration)
  }

  override fun finish() {
    vibrate()
    cancel()
  }

  override fun cancel() {
    properties = null
    hideNotification()
  }

  fun showNotification(title: String) {
    properties?.also {
      sendNotification(
        title = title,
        durationMillis = it.duration.inWholeMilliseconds - (System.currentTimeMillis() - it.startTime)
      )
    }
  }

  fun hideNotification() {
    with(NotificationManagerCompat.from(this)) {
      cancel(TimerServiceProperties.NOTIFICATION_ID)
    }
  }

  private fun sendNotification(title: String, durationMillis: Long) {
    val notificationBuilder =
      NotificationCompat.Builder(this, TimerServiceProperties.CHANNEL_ID).setContentTitle(title)
        .setSmallIcon(ic_lock_idle_alarm).setPriority(NotificationCompat.PRIORITY_LOW)
        .setOnlyAlertOnce(true).setUsesChronometer(true).setChronometerCountDown(true)
        .setWhen(System.currentTimeMillis() + durationMillis).setOngoing(true)

    with(NotificationManagerCompat.from(this)) {
      if (checkPermission()) {
        notify(TimerServiceProperties.NOTIFICATION_ID, notificationBuilder.build())
      }
    }
  }

  @Suppress("HardCodedStringLiteral")
  private fun vibrate() {
    val vibration = VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
    val attributes = VibrationAttributes.Builder().setUsage(VibrationAttributes.USAGE_ALARM).build()

    (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator.apply {
      hasVibrator().onTrue {
        // Vibration without attributes does not work, if the app is on the background
        vibrate(vibration, attributes)
      }.onFalse {
        Log.e(DebugTag.ERROR.name, "Device does not have a vibrator")
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
    fun getService(): CountdownTimerService = this@CountdownTimerService
  }
}