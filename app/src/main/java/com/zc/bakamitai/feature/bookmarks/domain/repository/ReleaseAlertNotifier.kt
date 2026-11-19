package com.zc.bakamitai.feature.bookmarks.domain.repository

import com.zc.bakamitai.feature.bookmarks.domain.model.EpisodeAlert

/** Posts new-episode notifications (a port: implemented in `presentation/notification/`). */
interface ReleaseAlertNotifier {

    /**
     * Whether notifications can be posted: the permission (Android 13+), the app's notifications and the channel
     * are on. The Settings feature will AND its own switch in here.
     */
    fun areAlertsEnabled(): Boolean

    /** Posts one notification per alert (and a group summary), when alerts are enabled. */
    fun notify(alerts: List<EpisodeAlert>)
}
