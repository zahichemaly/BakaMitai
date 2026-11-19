package com.zc.bakamitai.feature.bookmarks.presentation.notification

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.zc.bakamitai.common.ui.navigation.showDetailsIntent
import com.zc.bakamitai.common.ui.resource.DrawableResource
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.bookmarks.domain.model.EpisodeAlert
import com.zc.bakamitai.feature.bookmarks.domain.repository.ReleaseAlertNotifier
import com.zc.bakamitai.feature.bookmarks.presentation.mapper.toEpisodeAlertUIModel
import com.zc.bakamitai.feature.bookmarks.presentation.model.EpisodeAlertUIModel
import org.koin.core.annotation.Factory

/** The notification channel of new-episode alerts. */
const val CHANNEL_NEW_EPISODES = "new_episodes"

/** The group of new-episode alerts, summarized when two or more are shown. */
const val GROUP_NEW_EPISODES = "new_episodes"

/** The tag of one show's alert (its id is per show). */
const val TAG_NEW_EPISODE = "new_episode"

/** The tag and id of the group summary. */
const val TAG_NEW_EPISODES_SUMMARY = "new_episodes_summary"
const val SUMMARY_NOTIFICATION_ID = 0

/** Each alert keeps its summary line here, so a later summary can list the alerts still shown. */
private const val EXTRA_SUMMARY_LINE = "com.zc.bakamitai.extra.SUMMARY_LINE"
private const val MIN_ALERTS_FOR_SUMMARY = 2

/**
 * Posts new-episode notifications (docs/5-bookmarks.md §2.7): one per show (a newer episode replaces the show's
 * older one), opening the show's Details, plus an inbox-style summary while two or more are shown. Every text is
 * resolved here, from `UiText`.
 */
@Factory
class ReleaseAlertNotifierImpl(private val context: Context) : ReleaseAlertNotifier {

    private val manager = NotificationManagerCompat.from(context)

    override fun areAlertsEnabled(): Boolean {
        val channelBlocked =
            manager.getNotificationChannelCompat(CHANNEL_NEW_EPISODES)?.importance ==
                NotificationManagerCompat.IMPORTANCE_NONE
        return hasPermission() && manager.areNotificationsEnabled() && !channelBlocked
    }

    override fun notify(alerts: List<EpisodeAlert>) {
        if (alerts.isEmpty() || !areAlertsEnabled()) return
        ensureChannel()
        val models = alerts.map { it.toEpisodeAlertUIModel() }
        models.forEach(::post)
        postSummary(models)
    }

    private fun post(alert: EpisodeAlertUIModel) {
        val notification = NotificationCompat.Builder(context, CHANNEL_NEW_EPISODES)
            .setSmallIcon(DrawableResource.ic_notification)
            .setContentTitle(alert.title.asString(context))
            .setContentText(alert.text.asString(context))
            .setSubText(alert.subText?.asString(context))
            .setAutoCancel(true)
            .setGroup(GROUP_NEW_EPISODES)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    alert.notificationId,
                    context.showDetailsIntent(alert.slug),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
            .addExtras(Bundle().apply { putCharSequence(EXTRA_SUMMARY_LINE, alert.summaryLine.asString(context)) })
            .build()
        postIfPermitted(TAG_NEW_EPISODE, alert.notificationId, notification)
    }

    /**
     * The summary of [posted] and the alerts still shown from earlier checks, when there are two or more. Posting
     * is asynchronous, so the alerts just posted are listed from [posted], not from the active notifications.
     */
    private fun postSummary(posted: List<EpisodeAlertUIModel>) {
        val postedIds = posted.mapTo(mutableSetOf()) { it.notificationId }
        val earlierLines = manager.activeNotifications
            .filter { it.tag == TAG_NEW_EPISODE && it.id !in postedIds }
            .mapNotNull { it.notification.extras.getCharSequence(EXTRA_SUMMARY_LINE)?.toString() }
        val lines = posted.map { it.summaryLine.asString(context) } + earlierLines
        if (lines.size < MIN_ALERTS_FOR_SUMMARY) return
        val text = UiText.StringPlural(PluralResource.notification_summary, lines.size, listOf(lines.size))
            .asString(context)
        val style = NotificationCompat.InboxStyle().setSummaryText(text)
        lines.forEach(style::addLine)
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val notification = NotificationCompat.Builder(context, CHANNEL_NEW_EPISODES)
            .setSmallIcon(DrawableResource.ic_notification)
            .setContentTitle(text)
            .setContentText(lines.joinToString())
            .setStyle(style)
            .setAutoCancel(true)
            .setGroup(GROUP_NEW_EPISODES)
            .setGroupSummary(true)
            .setContentIntent(
                launchIntent?.let {
                    PendingIntent.getActivity(context, SUMMARY_NOTIFICATION_ID, it, PendingIntent.FLAG_IMMUTABLE)
                },
            )
            .build()
        postIfPermitted(TAG_NEW_EPISODES_SUMMARY, SUMMARY_NOTIFICATION_ID, notification)
    }

    /** Creates the channel, or updates its name and description (idempotent). */
    private fun ensureChannel() {
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_NEW_EPISODES, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(UiText.StringResource(StringResource.notification_channel_new_episodes).asString(context))
                .setDescription(
                    UiText.StringResource(StringResource.notification_channel_new_episodes_description)
                        .asString(context),
                )
                .build(),
        )
    }

    private fun postIfPermitted(tag: String, id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        manager.notify(tag, id, notification)
    }

    private fun hasPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
}
