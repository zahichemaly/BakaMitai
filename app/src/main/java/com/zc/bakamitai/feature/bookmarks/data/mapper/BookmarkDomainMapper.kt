package com.zc.bakamitai.feature.bookmarks.data.mapper

import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkAiringRow
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkEntity
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkTrackingRow
import com.zc.bakamitai.feature.bookmarks.domain.model.AiringSlot
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatchUpdate
import kotlinx.datetime.isoDayNumber
import kotlin.time.Instant

private const val MINUTES_PER_HOUR = 60

/**
 * A new row for [this] bookmark, tracking new episodes from [trackingSince], with no episode seen yet. Its slot,
 * if known (an Undo), is kept for display but marked as never fetched, so the next check refreshes it.
 */
fun Bookmark.toEntity(trackingSince: Instant): BookmarkEntity = BookmarkEntity(
    slug = show.slug.value,
    title = show.title,
    coverUrl = show.coverUrl,
    addedAt = addedAt.toEpochMilliseconds(),
    trackingSince = trackingSince.toEpochMilliseconds(),
    airingDay = airing?.dayOfWeek?.isoDayNumber,
    airingMinute = airing?.time?.let { it.hour * MINUTES_PER_HOUR + it.minute },
    airingZone = airing?.timeZone?.id,
    airingTitle = airing?.releaseTitle,
)

/** [slug]'s slot columns as fetched at [checkedAt]; all `null` when the show isn't on the weekly schedule. */
fun AiringSlot?.toAiringRow(slug: ShowSlug, checkedAt: Instant): BookmarkAiringRow = BookmarkAiringRow(
    slug = slug.value,
    airingDay = this?.dayOfWeek?.isoDayNumber,
    airingMinute = this?.time?.let { it.hour * MINUTES_PER_HOUR + it.minute },
    airingZone = this?.timeZone?.id,
    airingTitle = this?.releaseTitle,
    airingCheckedAt = checkedAt.toEpochMilliseconds(),
)

fun ReleaseWatchUpdate.toTrackingRow(): BookmarkTrackingRow = BookmarkTrackingRow(
    slug = slug.value,
    lastSeenEpisode = lastSeen?.key?.value,
    lastSeenReleasedAt = lastSeen?.releasedAt?.toEpochMilliseconds(),
    resolvedThrough = resolvedThrough?.toEpochMilliseconds(),
)
