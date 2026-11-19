package com.zc.bakamitai.feature.bookmarks.presentation

import com.zc.bakamitai.feature.bookmarks.presentation.model.BookmarkSortOption

sealed interface BookmarksIntent {

    /** From `init`: the sort chips, the alerts check and a silent slot refresh. */
    data object Load : BookmarksIntent

    /** The screen is shown again (back from Details or the system settings). */
    data object ScreenStarted : BookmarksIntent

    /** Pull-to-refresh: refetches the weekly schedule for the airing chips. */
    data object Refresh : BookmarksIntent

    /** The error view's retry: reads the bookmarks again. */
    data object Retry : BookmarksIntent

    data class SortSelected(val option: BookmarkSortOption) : BookmarksIntent

    data class BookmarkClicked(val slug: String) : BookmarksIntent

    data class RemoveClicked(val slug: String) : BookmarksIntent

    /** The removal snackbar's Undo. */
    data class UndoRemoveClicked(val slug: String) : BookmarksIntent

    /** The empty state's "Discover airing shows". */
    data object DiscoverShowsClicked : BookmarksIntent

    data object AlertsChipClicked : BookmarksIntent

    /** The notification permission dialog's answer. */
    data class AlertsPermissionResult(val granted: Boolean) : BookmarksIntent

    data object SettingsClicked : BookmarksIntent
}
