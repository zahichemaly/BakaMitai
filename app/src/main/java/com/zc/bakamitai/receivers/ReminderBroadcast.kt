package com.zc.bakamitai.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zc.bakamitai.R
import com.zc.bakamitai.ui.details.DetailsActivity
import com.zc.bakamitai.utils.NotificationHelper

class ReminderBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val requestCode = intent?.getIntExtra(NotificationHelper.REQUEST_CODE, 0) ?: 0
        val title = intent?.getStringExtra(NotificationHelper.TITLE) ?: context.getString(R.string.notif_title)
        val content = intent?.getStringExtra(NotificationHelper.CONTENT) ?: ""
        createNotification(context, requestCode, title, content)
    }

    private fun createNotification(context: Context, id: Int, title: String, content: String) {
        createNotificationChannel(context)

        val intent = Intent(context, DetailsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, id, intent, flags)

        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(content)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                NotificationHelper.CHANNEL_ID,
                context.getString(R.string.channel_name),
                importance
            ).apply {
                description = context.getString(R.string.channel_description)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
