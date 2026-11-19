package com.zc.bakamitai.feature.bookmarks.data.local

import androidx.room.ColumnInfo

/**
 * The weekly-slot columns of one [BookmarkEntity], for a partial update (`@Update(entity = BookmarkEntity::class)`).
 * Every airing column is `null` when the show isn't on the weekly schedule.
 */
data class BookmarkAiringRow(
    @ColumnInfo(name = "slug") val slug: String,
    @ColumnInfo(name = "airing_day") val airingDay: Int?,
    @ColumnInfo(name = "airing_minute") val airingMinute: Int?,
    @ColumnInfo(name = "airing_zone") val airingZone: String?,
    @ColumnInfo(name = "airing_title") val airingTitle: String?,
    @ColumnInfo(name = "airing_checked_at") val airingCheckedAt: Long,
)
