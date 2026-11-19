package com.zc.bakamitai.feature.details.data.mapper

import com.zc.bakamitai.common.data.mapper.toReleaseInstant
import com.zc.bakamitai.feature.details.data.dto.DownloadDTO
import com.zc.bakamitai.feature.details.data.dto.EpisodeDTO
import com.zc.bakamitai.feature.details.domain.model.Download
import com.zc.bakamitai.feature.details.domain.model.Episode
import com.zc.bakamitai.feature.details.domain.model.Resolution

/**
 * The episode, or `null` when it has no label or no parseable `release_date`. The label is kept as SubsPlease
 * writes it. An episode without usable downloads is kept: it's a real release whose links are missing.
 */
fun EpisodeDTO.toDomain(): Episode? {
    val label = episode?.trim()?.ifEmpty { null }
    val releasedAt = releaseDate?.toReleaseInstant()
    return if (label != null && releasedAt != null) {
        Episode(label = label, releasedAt = releasedAt, downloads = downloads.orEmpty().toDomain())
    } else {
        null
    }
}

/**
 * One download per resolution, in response order: the first one listed, except for SD, where the highest of
 * several heights wins (the first one listed among equals).
 */
private fun List<DownloadDTO>.toDomain(): List<Download> = mapNotNull { it.toDomain() }
    .groupBy { it.resolution }
    .map { (resolution, downloads) ->
        if (resolution == Resolution.SD) downloads.sortedByDescending { it.height }.first() else downloads.first()
    }
