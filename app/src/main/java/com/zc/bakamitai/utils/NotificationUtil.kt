package com.zc.bakamitai.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.zc.bakamitai.receivers.ReminderBroadcast

object NotificationUtil {
    private const val CHANNEL_ID = "1000"
    const val NOTIF_TITLE = "nt_title"
    const val NOTIF_CONTENT = "nt_content"
    const val NOTIF_ID = "nt_id"

    fun scheduleBookmarkNotification(context: Context) {
        val intent = Intent(context, ReminderBroadcast::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time: Long = System.currentTimeMillis() + 1000 * 10
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent)
    }
}
