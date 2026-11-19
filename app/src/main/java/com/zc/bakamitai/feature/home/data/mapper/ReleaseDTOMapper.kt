package com.zc.bakamitai.feature.home.data.mapper

import com.zc.bakamitai.common.data.mapper.toCoverUrl
import com.zc.bakamitai.common.data.mapper.toReleaseInstant
import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.home.data.dto.ReleaseDTO
import com.zc.bakamitai.feature.home.data.dto.ReleaseListDTO
import com.zc.bakamitai.feature.home.domain.model.Release

/**
 * `null` when the release has no slug, show, episode or parseable `release_date`. The episode is kept as-is
 * (`"07"`, `"01-24"`, `"Movie"`).
 */
fun ReleaseDTO.toDomain(): Release? {
    val slug = page?.takeIf { it.isNotBlank() }
    val title = show?.takeIf { it.isNotBlank() }
    val releasedShow = if (slug != null && title != null) {
        Show(slug = ShowSlug(slug), title = title, coverUrl = imageUrl.toCoverUrl())
    } else {
        null
    }
    val episode = episode?.trim()?.takeIf { it.isNotEmpty() }
    val releasedAt = releaseDate?.toReleaseInstant()
    return if (releasedShow != null && episode != null && releasedAt != null) {
        Release(show = releasedShow, episode = episode, releasedAt = releasedAt)
    } else {
        null
    }
}

fun ReleaseListDTO.toDomain(): List<Release> = releases.mapNotNull { it.toDomain() }
