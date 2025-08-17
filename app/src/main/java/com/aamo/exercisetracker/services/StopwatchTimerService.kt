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
import com.aamo.exercisetracker.utility.tags.ERROR_TAG
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class StopwatchTimerService() : Service() {
  /**
   * @param onFinished Will be called when the timer has been stopped
   * @param onCleanUp Will be called when the timer has been stopped or cancelled
   */
  private data class TimerState(
    val onFinished: (Duration) -> Unit,
    val onCleanUp: (() -> Unit)?,
    val startTime: Long,
  )

  private val binder = BinderHelper()
  private var state: TimerState? = null

  override fun onBind(p0: Intent?): IBinder? {
    return binder
  }

  override fun onTaskRemoved(rootIntent: Intent?) {
    super.onTaskRemoved(rootIntent)
    // Stop this service when the activity has been closed
    //  works only if this service has been started with startService once
    cancel()
    stopSelf()
  }

  fun start(
    onStart: (() -> Unit)? = null, onFinished: (Duration) -> Unit, onCleanUp: (() -> Unit)? = null
  ) {
    cancel()
    state = TimerState(
      onFinished = onFinished,
      onCleanUp = onCleanUp,
      startTime = System.currentTimeMillis(),
    )
    onStart?.invoke()
  }

  fun stop() {
    state?.also {
      // Cancel before invoking so the onFinished can start a new timer
      cancel()
      it.onFinished.invoke((System.currentTimeMillis() - it.startTime).milliseconds)
    }
  }

  fun cancel() {
    state?.apply {
      onCleanUp?.invoke()
    }

    state = null
    hideNotification()
  }

  fun showNotification(title: String) {
    if (state == null) return

    state?.also {
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
      Log.e(ERROR_TAG, "Permission denied")
    }
  }

  inner class BinderHelper : Binder() {
    fun getService(): StopwatchTimerService = this@StopwatchTimerService
  }
}