package com.zc.bakamitai.feature.home.presentation

sealed interface HomeIntent {

    /** The first load, from `init`. */
    data object Load : HomeIntent

    /** The full-screen error's Retry button. */
    data object Retry : HomeIntent

    /** Pull-to-refresh. */
    data object Refresh : HomeIntent

    /** The screen reached STARTED: reload if the local date changed since the last load. */
    data object ScreenStarted : HomeIntent

    data class ShowClicked(val slug: String) : HomeIntent

    data object SettingsClicked : HomeIntent
}
