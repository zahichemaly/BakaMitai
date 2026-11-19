package com.zc.bakamitai.feature.settings.presentation.component.alertspreference.mapper

import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseAlertsStatus
import com.zc.bakamitai.feature.settings.presentation.component.alertspreference.model.AlertsPreferenceUIState

/**
 * The switch from the saved setting, blocked only when it's on and Android blocks notifications. [status] is `null`
 * until it's been read: not blocked.
 */
fun AppSettings.toAlertsPreferenceUIState(status: ReleaseAlertsStatus?): AlertsPreferenceUIState {
    val isBlocked = newEpisodeAlerts && status == ReleaseAlertsStatus.BLOCKED
    return AlertsPreferenceUIState(
        isChecked = newEpisodeAlerts,
        supportingText = UiText.StringResource(
            if (isBlocked) StringResource.settings_alerts_blocked else StringResource.settings_alerts_description,
        ),
        isBlocked = isBlocked,
    )
}
