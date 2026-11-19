package com.zc.bakamitai.feature.bookmarks.data.local

import androidx.room.ColumnInfo

/** The new-episode tracking columns of one [BookmarkEntity], for a partial update after a release check. */
data class BookmarkTrackingRow(
    @ColumnInfo(name = "slug") val slug: String,
    @ColumnInfo(name = "last_seen_episode") val lastSeenEpisode: String?,
    @ColumnInfo(name = "last_seen_released_at") val lastSeenReleasedAt: Long?,
    @ColumnInfo(name = "resolved_through") val resolvedThrough: Long?,
)
