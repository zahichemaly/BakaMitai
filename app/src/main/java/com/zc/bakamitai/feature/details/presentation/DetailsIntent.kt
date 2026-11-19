package com.zc.bakamitai.feature.details.presentation

import com.zc.bakamitai.feature.details.presentation.model.DownloadSource

sealed interface DetailsIntent {

    /** The first load, from the ViewModel's `init`. */
    data object Load : DetailsIntent

    /** The full-screen error's retry. */
    data object Retry : DetailsIntent

    /** The downloads section's retry, with the header shown. */
    data object RetryEpisodes : DetailsIntent

    /** Pull-to-refresh: reloads the page and the episodes, bypassing the cache. */
    data object Refresh : DetailsIntent

    data object BackClicked : DetailsIntent

    data object ShareClicked : DetailsIntent

    data object OpenInWebClicked : DetailsIntent

    data object BookmarkClicked : DetailsIntent

    data object SynopsisToggled : DetailsIntent

    data class EpisodeToggled(val key: String) : DetailsIntent

    data class SourceClicked(val source: DownloadSource, val url: String) : DetailsIntent
}
