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
import java.util.Timer
import kotlin.concurrent.timerTask

interface ICountdownTimerService {
  fun start(
    durationMillis: Long, onFinished: () -> Unit, onStart: (() -> Unit)?, onCleanUp: (() -> Unit)?
  ) {
  }

  fun stop() {}
  fun cancel() {}
}

class CountdownTimerService() : Service(), ICountdownTimerService {
  /**
   * @param onFinished Will be called when the timer has been stopped
   * @param onCleanUp Will be called when the timer has been stopped or cancelled
   */
  private data class TimerState(
    val timer: Timer,
    val onFinished: () -> Unit,
    val onCleanUp: (() -> Unit)?,
    val startTime: Long,
    val durationMillis: Long,
  )

  private val binder = BinderHelper()
  private var state: TimerState? = null

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

  override fun start(
    durationMillis: Long, onFinished: () -> Unit, onStart: (() -> Unit)?, onCleanUp: (() -> Unit)?
  ) {
    cancel()
    state = TimerState(
      timer = Timer(true).apply {
        schedule(timerTask {
          // OnFinished and onCleanUp will be invoked in the stop() function
          vibrate()
          stop()
        }, durationMillis)
      },
      onFinished = onFinished,
      onCleanUp = onCleanUp,
      startTime = System.currentTimeMillis(),
      durationMillis = durationMillis
    )
    onStart?.invoke()
  }

  override fun stop() {
    state?.onFinished.let {
      // Cancel before invoking so the onFinished can start a new timer
      cancel()
      it?.invoke()
    }
  }

  override fun cancel() {
    state?.apply {
      timer.cancel()
      timer.purge()
      onCleanUp?.invoke()
    }

    state = null
    hideNotification()
  }

  fun showNotification(title: String) {
    if (state == null) return

    state?.let {
      sendNotification(
        title = title,
        durationMillis = it.durationMillis - (System.currentTimeMillis() - it.startTime)
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