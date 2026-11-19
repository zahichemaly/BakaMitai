package com.zc.bakamitai.feature.bookmarks.presentation

import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.text.UiText

sealed interface BookmarksEffect {

    data class OpenDetails(val slug: String) : BookmarksEffect

    data object OpenSchedule : BookmarksEffect

    /** The settings gear, or the alerts chip while alerts are on or switched off in Settings. */
    data object OpenSettings : BookmarksEffect

    /** "Removed Frieren" with Undo, which sends `UndoRemoveClicked(slug)`. */
    data class ShowRemoved(val message: UiText, val slug: String) : BookmarksEffect

    /**
     * Android blocks alerts: the permission dialog while Android still shows it, otherwise the notification
     * settings.
     */
    data object EnableAlerts : BookmarksEffect

    /** The permission was denied: a snackbar with a shortcut to the notification settings. */
    data object ShowAlertsDenied : BookmarksEffect

    /** A refresh, a removal or an Undo failed. */
    data class ShowError(val error: ErrorUIModel) : BookmarksEffect
}
