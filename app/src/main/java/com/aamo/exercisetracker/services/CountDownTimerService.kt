package com.aamo.exercisetracker.services

import android.Manifest
import android.R.drawable.ic_lock_idle_alarm
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.aamo.exercisetracker.utility.extensions.general.onFalse
import com.aamo.exercisetracker.utility.extensions.general.onTrue
import java.util.Timer
import kotlin.concurrent.timerTask

class CountDownTimerService() : Service() {
  private data class TimerState(
    val title: String,
    val timer: Timer,
    val onFinished: (() -> Unit),
    val startTime: Long,
    val durationMillis: Long,
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

  fun start(title: String, durationMillis: Long, onFinished: () -> Unit) {
    if (durationMillis > 0L) {
      cancel()
      state = TimerState(
        title = title,
        timer = Timer(true).apply {
          schedule(timerTask {
            vibrate()
            stop()
          }, durationMillis)
        },
        onFinished = onFinished,
        startTime = System.currentTimeMillis(),
        durationMillis = durationMillis
      )
    }
  }

  fun stop() {
    state?.onFinished.let {
      // Cancel before invoking so the onFinished can start a new timer
      cancel()
      it?.invoke()
    }
  }

  fun cancel() {
    state?.apply {
      timer.cancel()
      timer.purge()
    }
    state = null

    hideNotification()
  }

  fun showNotification() {
    if (state == null) return

    state?.let {
      sendNotification(
        title = it.title,
        durationMillis = it.durationMillis - (System.currentTimeMillis() - it.startTime)
      )
    }
  }

  fun hideNotification() {
    with(NotificationManagerCompat.from(this)) {
      cancel(NOTIFICATION_ID)
    }
  }

  private fun sendNotification(title: String, durationMillis: Long) {
    var notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle(title)
      .setSmallIcon(ic_lock_idle_alarm).setPriority(NotificationCompat.PRIORITY_LOW)
      .setOnlyAlertOnce(true).setUsesChronometer(true).setChronometerCountDown(true)
      .setWhen(System.currentTimeMillis() + durationMillis).setOngoing(true)

    with(NotificationManagerCompat.from(this)) {
      if (checkPermission()) {
        notify(NOTIFICATION_ID, notificationBuilder.build())
      }
    }
  }

  private fun vibrate() {
    val vibration = VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
    val attributes = VibrationAttributes.Builder().setUsage(VibrationAttributes.USAGE_ALARM).build()

    (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator.apply {
      hasVibrator().onTrue {
        // Vibration without attributes does not work, if the app is on the background
        vibrate(vibration, attributes)
      }.onFalse { Log.e("asd", "Device does not have a vibrator") }
    }
  }

  private fun checkPermission(): Boolean {
    return (ActivityCompat.checkSelfPermission(
      this, Manifest.permission.POST_NOTIFICATIONS
    ) != PackageManager.PERMISSION_DENIED).onFalse {
      Log.e("asd", "Permission denied")
    }
  }

  inner class BinderHelper : Binder() {
    fun getService(): CountDownTimerService = this@CountDownTimerService
  }

  companion object {
    const val PERMISSION_CODE: Int = 1
    private const val CHANNEL_ID: String = "CountDownTimer channel"
    private const val NOTIFICATION_ID: Int = 0

    fun createNotificationChannel(context: Context, activity: Activity) {
      with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
          ) == PackageManager.PERMISSION_DENIED) {

          requestPermissions(
            activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), PERMISSION_CODE
          )

          return@with
        }
        else {
          val name = "name"
          val importance = NotificationManager.IMPORTANCE_LOW
          val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = "desc"
          }

          val notificationManager =
            getSystemService(context, NotificationManager::class.java) as NotificationManager

          notificationManager.createNotificationChannel(channel)
        }
      }
    }
  }
}