package com.zc.bakamitai.feature.shows.presentation

sealed interface AllShowsIntent {

    /** The first load, from `init`. */
    data object Load : AllShowsIntent

    /** The full-screen error's Retry button. */
    data object Retry : AllShowsIntent

    /** Pull-to-refresh: bypasses the catalogue cache. */
    data object Refresh : AllShowsIntent

    /** The search field's text changed. */
    data class QueryChanged(val query: String) : AllShowsIntent

    /** The search field's clear button. */
    data object QueryCleared : AllShowsIntent

    data class ShowClicked(val slug: String) : AllShowsIntent

    data object SettingsClicked : AllShowsIntent
}
