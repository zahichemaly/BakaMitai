package com.zc.bakamitai.feature.bookmarks.domain.model

import kotlin.time.Instant

/**
 * What a release check found: the tracking [updates] to save, then the [alerts] to post, and when to run again
 * ([nextCheckAt], `null` = nothing to schedule).
 */
data class ReleaseCheckOutcome(
    val updates: List<ReleaseWatchUpdate>,
    val alerts: List<EpisodeAlert>,
    val nextCheckAt: Instant?,
)
