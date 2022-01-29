package com.zc.bakamitai.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.zc.bakamitai.R
import com.zc.bakamitai.data.room.entities.Schedule
import com.zc.bakamitai.receivers.ReminderBroadcast
import timber.log.Timber
import java.util.*

object NotificationHelper {
    const val CHANNEL_ID = "1000"
    const val REQUEST_CODE = "request_code"
    const val TITLE = "nt_title"
    const val CONTENT = "nt_content"
    const val PAGE = "nt_page"

    fun addNotification(context: Context, schedule: Schedule) {
        val requestCode = schedule.id
        val intent = Intent(context, ReminderBroadcast::class.java).apply {
            putExtra(REQUEST_CODE, requestCode)
            putExtra(TITLE, context.getString(R.string.notif_title))
            putExtra(CONTENT, context.getString(R.string.notif_content, schedule.name))
            putExtra(PAGE, schedule.page)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)
        val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        //val time: Long = System.currentTimeMillis() + 1000 * 10
        val time = schedule.date
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        Timber.d("Added notification for $requestCode on ${Date(time)}")
    }

    fun removeNotification(context: Context, requestCode: Int) {
        val intent = Intent(context, ReminderBroadcast::class.java).apply {
            putExtra(REQUEST_CODE, requestCode)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)
        pendingIntent.cancel()
        val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        Timber.d("Removed notification for $requestCode")
    }

    fun isNotificationSet(context: Context, requestCode: Int): Boolean {
        val intent = Intent(context, ReminderBroadcast::class.java).apply {
            putExtra(REQUEST_CODE, requestCode)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_NO_CREATE + PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE
        }
        val isPending = PendingIntent.getBroadcast(context, requestCode, intent, flags) != null
        Timber.d("Is notification pending for $requestCode? $isPending")
        return isPending
    }
}
