package com.zc.bakamitai.feature.bookmarks.domain.model

import kotlin.time.Instant

/** The newest release of a bookmarked show the release checks have seen, by episode key and `release_date`. */
data class SeenEpisode(val key: EpisodeKey, val releasedAt: Instant)
