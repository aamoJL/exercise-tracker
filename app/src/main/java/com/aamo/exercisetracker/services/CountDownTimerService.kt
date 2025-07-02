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
  private val binder = BinderHelper()
  private var timer: Timer? = null
  var title: String = "Timer"

  override fun onBind(p0: Intent?): IBinder? {
    return binder
  }

  override fun onTaskRemoved(rootIntent: Intent?) {
    super.onTaskRemoved(rootIntent)
    Log.i("asd", "Task removed")
    // Stop this service when the activity has been closed
    //  works only if this service has been started with startService once
    stop()
    stopSelf()
  }

  fun start(durationMillis: Long, onFinished: () -> Unit) {
    if (durationMillis > 0L) {
      startTimer(durationMillis = durationMillis, onFinished = onFinished)
      sendNotification(durationMillis)
    }
  }

  fun stop() {
    Log.i("asd", "stop")
    timer?.apply {
      cancel()
      purge()
    }
    timer = null
    removeNotification()
  }

  private fun startTimer(durationMillis: Long, onFinished: () -> Unit) {
    timer?.cancel()
    timer = Timer(true).apply {
      schedule(timerTask {
        vibrate()
        onFinished()
        stop()
      }, durationMillis)
    }
  }

  private fun sendNotification(durationMillis: Long) {
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
    // TODO: this does not work when the app is not active
    val vibration = VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)

    (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator.apply {
      hasVibrator().onTrue {
        vibrate(vibration)
      }.onFalse { Log.e("asd", "Device does not have a vibrator") }
    }
  }

  private fun removeNotification() {
    with(NotificationManagerCompat.from(this)) {
      cancel(NOTIFICATION_ID)
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
          // Register the channel with the system.
          val notificationManager: NotificationManager =
            getSystemService(context, NotificationManager::class.java) as NotificationManager

          notificationManager.createNotificationChannel(channel)
        }
      }
    }
  }
}