package com.zc.bakamitai.feature.bookmarks.data.mapper

import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkEntity
import com.zc.bakamitai.feature.bookmarks.domain.model.AiringSlot
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.model.EpisodeKey
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatch
import com.zc.bakamitai.feature.bookmarks.domain.model.SeenEpisode
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.IllegalTimeZoneException
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR
private val IsoDays = 1..7

/** The saved show, or `null` for a row without a slug or a title, which couldn't be opened or shown. */
fun BookmarkEntity.toBookmark(): Bookmark? = toShow()?.let { show ->
    Bookmark(show = show, addedAt = Instant.fromEpochMilliseconds(addedAt), airing = toAiringSlot())
}

/** The row's new-episode tracking, or `null` for a row without a slug or a title. */
fun BookmarkEntity.toReleaseWatch(): ReleaseWatch? = toShow()?.let { show ->
    ReleaseWatch(
        show = show,
        trackingSince = Instant.fromEpochMilliseconds(trackingSince),
        airing = toAiringSlot(),
        airingCheckedAt = airingCheckedAt?.let(Instant::fromEpochMilliseconds),
        lastSeen = toSeenEpisode(),
        resolvedThrough = resolvedThrough?.let(Instant::fromEpochMilliseconds),
    )
}

private fun BookmarkEntity.toShow(): Show? = if (slug.isBlank() || title.isBlank()) {
    null
} else {
    Show(slug = ShowSlug(slug), title = title, coverUrl = coverUrl)
}

/** A slot only when every airing column is set and valid; an unknown zone id or an out-of-range value is none. */
private fun BookmarkEntity.toAiringSlot(): AiringSlot? {
    val day = airingDay?.takeIf { it in IsoDays }?.let(::DayOfWeek)
    val time = airingMinute
        ?.takeIf { it in 0 until MINUTES_PER_DAY }
        ?.let { LocalTime(hour = it / MINUTES_PER_HOUR, minute = it % MINUTES_PER_HOUR) }
    val title = airingTitle?.takeIf { it.isNotBlank() }
    return airingZone?.toTimeZoneOrNull()?.let { zone ->
        if (day != null && time != null && title != null) {
            AiringSlot(dayOfWeek = day, time = time, timeZone = zone, releaseTitle = title)
        } else {
            null
        }
    }
}

private fun BookmarkEntity.toSeenEpisode(): SeenEpisode? {
    val episode = lastSeenEpisode?.takeIf { it.isNotBlank() }
    val releasedAt = lastSeenReleasedAt
    return if (episode == null || releasedAt == null) {
        null
    } else {
        SeenEpisode(key = EpisodeKey(episode), releasedAt = Instant.fromEpochMilliseconds(releasedAt))
    }
}

private fun String.toTimeZoneOrNull(): TimeZone? = try {
    TimeZone.of(this)
} catch (ignored: IllegalTimeZoneException) {
    null
}
