package com.zc.bakamitai.feature.bookmarks.presentation.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.common.ui.navigation.showDetailsIntent
import com.zc.bakamitai.feature.bookmarks.domain.model.EpisodeAlert
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * Real notifications, with the permission granted. The last test blocks the channel, which can't be undone
 * without reinstalling the app, so the methods run in name order.
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ReleaseAlertNotifierImplTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val manager = context.getSystemService(NotificationManager::class.java)
    private val notifier = ReleaseAlertNotifierImpl(context)
    private val releasedAt = Instant.parse("2026-11-16T11:00:00Z")

    private fun alert(slug: String, title: String, episode: String) =
        EpisodeAlert(Show(ShowSlug(slug), title, null), episode, releasedAt, newEpisodeCount = 1)

    /** The shown notifications once [isReady] holds (posting is asynchronous), or after 3 seconds. */
    private fun shown(isReady: (List<StatusBarNotification>) -> Boolean = { true }): List<StatusBarNotification> {
        repeat(POLL_ATTEMPTS) {
            val current = manager.activeNotifications.toList()
            if (isReady(current)) return current
            SystemClock.sleep(POLL_INTERVAL_MILLIS)
        }
        return manager.activeNotifications.toList()
    }

    @Before
    fun setUp() {
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .grantRuntimePermission(context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        manager.cancelAll()
    }

    @After
    fun tearDown() {
        manager.cancelAll()
    }

    @Test
    fun test1_oneAlertPostsOneNotificationThatOpensTheShow() {
        assertTrue(notifier.areAlertsEnabled())

        notifier.notify(listOf(alert("sousou-no-frieren", "Sousou no Frieren", "08")))

        val posted = shown { it.isNotEmpty() }.single()
        assertEquals(TAG_NEW_EPISODE, posted.tag)
        assertEquals(
            "Sousou no Frieren",
            posted.notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
        )
        assertEquals(
            "Episode 08 is out",
            posted.notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
        )
        assertEquals(GROUP_NEW_EPISODES, posted.notification.group)
        assertEquals(CHANNEL_NEW_EPISODES, posted.notification.channelId)
        assertNotNull(posted.notification.contentIntent)
        // The same activity intent and request code exist: the tap opens this show's Details.
        assertNotNull(
            PendingIntent.getActivity(
                context,
                posted.id,
                context.showDetailsIntent("sousou-no-frieren"),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            ),
        )
    }

    @Test
    fun test2_twoAlertsPostTwoNotificationsAndASummary() {
        notifier.notify(
            listOf(
                alert("sousou-no-frieren", "Sousou no Frieren", "08"),
                alert("dandadan", "Dandadan", "Movie"),
            ),
        )

        val posted = shown { it.size == EXPECTED_WITH_SUMMARY }
        assertEquals(2, posted.count { it.tag == TAG_NEW_EPISODE })
        val summary = posted.single { it.tag == TAG_NEW_EPISODES_SUMMARY }
        assertTrue(summary.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0)
        assertEquals(
            "2 new episodes",
            summary.notification.extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString(),
        )
        val lines = summary.notification.extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.map(CharSequence::toString)
            .orEmpty()
        assertEquals(setOf("Sousou no Frieren · Episode 08", "Dandadan · Movie"), lines.toSet())
    }

    @Test
    fun test3_aBlockedChannelPostsNothing() {
        notifier.notify(listOf(alert("sousou-no-frieren", "Sousou no Frieren", "08")))
        shown { it.isNotEmpty() }
        manager.cancelAll()
        shown { it.isEmpty() }
        val channel = manager.getNotificationChannel(CHANNEL_NEW_EPISODES)
        manager.createNotificationChannel(
            NotificationChannel(channel.id, channel.name, NotificationManager.IMPORTANCE_NONE),
        )

        assertFalse(notifier.areAlertsEnabled())
        notifier.notify(listOf(alert("dandadan", "Dandadan", "07")))
        SystemClock.sleep(POLL_INTERVAL_MILLIS * POLL_ATTEMPTS / 2)

        assertEquals(emptyList(), shown())
    }

    private companion object {
        const val POLL_ATTEMPTS = 60
        const val POLL_INTERVAL_MILLIS = 50L
        const val EXPECTED_WITH_SUMMARY = 3
    }
}
