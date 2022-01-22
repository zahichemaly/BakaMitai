package com.zc.bakamitai.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.zc.bakamitai.receivers.ReminderBroadcast

object NotificationHelper {
    const val CHANNEL_ID = "1000"
    const val REQUEST_CODE = "request_code"
    const val TITLE = "nt_title"
    const val CONTENT = "nt_content"
    const val ID = "nt_id"

    fun scheduleBookmarkNotification(context: Context, requestCode: Int, seconds: Int) {
        val intent = Intent(context, ReminderBroadcast::class.java)
        intent.putExtra(REQUEST_CODE, requestCode)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE
        else 0
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)
        val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time: Long = System.currentTimeMillis() + 1000 * seconds
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent)
    }
}
