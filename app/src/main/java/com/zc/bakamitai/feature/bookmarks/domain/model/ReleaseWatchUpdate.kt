package com.zc.bakamitai.feature.bookmarks.domain.model

import com.zc.bakamitai.common.domain.model.ShowSlug
import kotlin.time.Instant

/** The tracking state a release check saves for one bookmark. */
data class ReleaseWatchUpdate(val slug: ShowSlug, val lastSeen: SeenEpisode?, val resolvedThrough: Instant?)
