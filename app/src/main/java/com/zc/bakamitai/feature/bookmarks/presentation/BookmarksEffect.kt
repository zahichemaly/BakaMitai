package com.zc.bakamitai.feature.bookmarks.presentation

import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.text.UiText

sealed interface BookmarksEffect {

    data class OpenDetails(val slug: String) : BookmarksEffect

    data object OpenSchedule : BookmarksEffect

    data object OpenSettings : BookmarksEffect

    /** "Removed Frieren" with Undo, which sends `UndoRemoveClicked(slug)`. */
    data class ShowRemoved(val message: UiText, val slug: String) : BookmarksEffect

    /** Turn alerts on: the permission dialog while Android still shows it, otherwise the notification settings. */
    data object EnableAlerts : BookmarksEffect

    data object OpenNotificationSettings : BookmarksEffect

    /** The permission was denied: a snackbar with a shortcut to the notification settings. */
    data object ShowAlertsDenied : BookmarksEffect

    /** A refresh, a removal or an Undo failed. */
    data class ShowError(val error: ErrorUIModel) : BookmarksEffect
}
