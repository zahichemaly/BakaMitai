package com.zc.bakamitai.feature.bookmarks.domain.repository

import com.zc.bakamitai.feature.bookmarks.domain.model.EpisodeAlert

/** Posts new-episode notifications (a port: implemented in `presentation/notification/`). */
interface ReleaseAlertNotifier {

    /**
     * Whether Android lets the app post notifications: the permission (Android 13+), the app's notifications and the
     * channel are on. The in-app switch (Settings) is combined with it by `GetReleaseAlertsStatusUseCase`.
     */
    fun areAlertsEnabled(): Boolean

    /** Posts one notification per alert (and a group summary), when alerts are enabled. */
    fun notify(alerts: List<EpisodeAlert>)
}
