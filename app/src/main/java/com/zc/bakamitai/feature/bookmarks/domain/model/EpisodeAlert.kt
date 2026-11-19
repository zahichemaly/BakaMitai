package com.zc.bakamitai.feature.bookmarks.domain.model

import com.zc.bakamitai.common.domain.model.Show
import kotlin.time.Instant

/**
 * A new episode to notify about: one per show and check.
 *
 * @property episode the newest new episode's label as SubsPlease writes it ("08", "Movie", "Egghead SP11").
 * @property newEpisodeCount how many new episodes the check found for the show (more than one after a backlog).
 */
data class EpisodeAlert(val show: Show, val episode: String, val releasedAt: Instant, val newEpisodeCount: Int)
