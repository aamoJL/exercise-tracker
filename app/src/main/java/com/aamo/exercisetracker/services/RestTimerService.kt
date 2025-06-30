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
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class RestTimerService() : Service() {
  private val binder = LocalBinder()

  inner class LocalBinder : Binder() {
    fun getService(): RestTimerService = this@RestTimerService
  }

  override fun onBind(p0: Intent?): IBinder? {
    Log.d("asd", "Bind")
    return null
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val duration = intent?.getLongExtra(DURATION_EXTRA_KEY, 0L)?.milliseconds ?: 0.milliseconds

    if (duration > 0.milliseconds) {
      startTimer(duration = duration, onCompleted = {
        stopSelf()
      })
    }

    return super.onStartCommand(intent, flags, startId)
  }

  override fun onDestroy() {
    Log.d("asd", "Destroy")
    super.onDestroy()
  }

  private fun startTimer(duration: Duration, onCompleted: () -> Unit) {
    object : CountDownTimer(duration.inWholeMilliseconds, 1.seconds.inWholeMilliseconds) {
      override fun onTick(millisUntilFinished: Long) {
        notify(millisUntilFinished.milliseconds)
      }

      override fun onFinish() {
        cancel()
        onCompleted()
      }
    }.start()
  }

  private fun notify(duration: Duration) {
    var notificationBuilder =
      NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Rest timer")
        .setSmallIcon(ic_lock_idle_alarm).setContentText(duration.inWholeSeconds.seconds.toString())
        .setPriority(NotificationCompat.PRIORITY_LOW).setOnlyAlertOnce(true)

    with(NotificationManagerCompat.from(this)) {
      if (checkPermission()) {
        notify(NOTIFICATION_ID, notificationBuilder.build())
      }
    }
  }

  private fun checkPermission(): Boolean {
    if (ActivityCompat.checkSelfPermission(
        this, Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_DENIED) {
      Log.e("asd", "Permission denied")

      return false
    }
    return true
  }

  companion object {
    const val CHANNEL_ID: String = "Rest timer channel"
    const val NOTIFICATION_ID: Int = 0
    const val PERMISSION_CODE: Int = 1
    const val DURATION_EXTRA_KEY: String = "duration"

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