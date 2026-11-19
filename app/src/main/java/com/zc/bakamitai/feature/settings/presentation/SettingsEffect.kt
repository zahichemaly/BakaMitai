package com.zc.bakamitai.feature.settings.presentation

import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel

sealed interface SettingsEffect {

    data object NavigateBack : SettingsEffect

    /** The source code page, in the browser. Without an app for it: "No app can open this link." */
    data class OpenUrl(val url: String) : SettingsEffect

    /**
     * Android blocks the app's notifications: the permission dialog while Android still shows it, otherwise the
     * app's notification settings.
     */
    data object RequestNotifications : SettingsEffect

    /** A setting couldn't be saved. */
    data class ShowError(val error: ErrorUIModel) : SettingsEffect
}
