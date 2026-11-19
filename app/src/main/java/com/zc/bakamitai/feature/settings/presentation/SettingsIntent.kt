package com.zc.bakamitai.feature.settings.presentation

import com.zc.bakamitai.feature.settings.presentation.model.ThemeOption
import com.zc.bakamitai.feature.settings.presentation.model.TimeFormatOption
import com.zc.bakamitai.feature.settings.presentation.model.WeekStartOption

sealed interface SettingsIntent {

    /** The screen reached STARTED (first shown, or back from the system settings): re-reads the alerts status. */
    data object ScreenStarted : SettingsIntent

    data object BackClicked : SettingsIntent

    data class WeekStartSelected(val option: WeekStartOption) : SettingsIntent

    data class TimeFormatSelected(val option: TimeFormatOption) : SettingsIntent

    data class ThemeSelected(val option: ThemeOption) : SettingsIntent

    data class AlertsToggled(val enabled: Boolean) : SettingsIntent

    /** The blocked alerts row's "Allow notifications". */
    data object AllowNotificationsClicked : SettingsIntent

    /** The notification permission dialog's answer. */
    data class NotificationPermissionResult(val granted: Boolean) : SettingsIntent

    data object AboutClicked : SettingsIntent
}
