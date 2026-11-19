package com.zc.bakamitai.feature.details.domain.model

import kotlinx.datetime.TimeZone

/**
 * A show's downloads.
 *
 * @property isTruncated SubsPlease lists at most [EPISODE_LIMIT] episodes, and this show hit that cap: older ones
 * exist but can't be listed.
 * @property timeZone the zone to show release times in.
 */
data class ShowEpisodes(
    val episodes: List<Episode>,
    val batches: List<Episode>,
    val isTruncated: Boolean,
    val timeZone: TimeZone,
) {
    companion object {
        /** SubsPlease lists at most this many episodes of a show, newest first (docs/4-details.md D1). */
        const val EPISODE_LIMIT = 52
    }
}
