package com.zc.bakamitai.feature.home.data.mapper

import com.zc.bakamitai.feature.home.data.dto.ReleaseDTO
import com.zc.bakamitai.feature.home.data.dto.ReleaseListDTO
import com.zc.bakamitai.feature.home.domain.model.Release
import com.zc.bakamitai.feature.home.domain.model.Show
import com.zc.bakamitai.feature.home.domain.model.ShowSlug
import kotlinx.datetime.format.DateTimeComponents
import kotlin.time.Instant

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

/**
 * `release_date` is RFC 1123 (`"Sun, 15 Nov 2026 10:03:53 -0500"`) with an offset that varies per endpoint and
 * per item, so it is always read as an instant. Values that parse but don't form a valid date (a day that
 * doesn't exist, a weekday that doesn't match) are `null` too.
 */
private fun String.toReleaseInstant(): Instant? = DateTimeComponents.Formats.RFC_1123.parseOrNull(trim())
    ?.takeIf { it.offsetHours != null }
    ?.let { components -> runCatching { components.toInstantUsingOffset() }.getOrNull() }
