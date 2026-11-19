package com.zc.bakamitai.feature.bookmarks.domain.repository

import com.zc.bakamitai.feature.bookmarks.domain.model.EpisodeAlert

/** Alerts are [enabled] until a test turns them off; every [notify] call is recorded in [posted]. */
class FakeReleaseAlertNotifier : ReleaseAlertNotifier {

    var enabled = true
    val posted = mutableListOf<List<EpisodeAlert>>()

    override fun areAlertsEnabled(): Boolean = enabled

    override fun notify(alerts: List<EpisodeAlert>) {
        posted += alerts
    }
}
