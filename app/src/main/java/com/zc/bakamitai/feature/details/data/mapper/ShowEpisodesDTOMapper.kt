package com.zc.bakamitai.feature.details.data.mapper

import com.zc.bakamitai.feature.details.data.dto.ShowEpisodesDTO
import com.zc.bakamitai.feature.details.domain.model.ShowEpisodes
import kotlinx.datetime.TimeZone

/**
 * The usable episodes and batches in response order, with [timeZone] attached. The list counts as truncated when
 * SubsPlease returned as many entries as it ever does, counted before unusable entries are dropped.
 */
fun ShowEpisodesDTO.toDomain(timeZone: TimeZone): ShowEpisodes {
    val rawEpisodes = episodes.orEmpty()
    return ShowEpisodes(
        episodes = rawEpisodes.mapNotNull { it.toDomain() },
        batches = batches.orEmpty().mapNotNull { it.toDomain() },
        isTruncated = rawEpisodes.size >= ShowEpisodes.EPISODE_LIMIT,
        timeZone = timeZone,
    )
}
