package com.zc.bakamitai.feature.bookmarks.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One saved show and its new-episode tracking (docs/5-bookmarks.md §4.3). Primitives only: instants are epoch
 * milliseconds, the weekday is ISO (1 = Monday), the zone an IANA id.
 *
 * @property trackingSince alerts consider releases after it: [addedAt], or the time of an Undo.
 * @property airingDay the weekly slot's ISO weekday; `null` when the show isn't on the weekly schedule.
 * @property airingMinute the slot's minutes after midnight in [airingZone].
 * @property airingTitle the weekly schedule's title, used for the per-show search.
 * @property airingCheckedAt when the slot was last refreshed; `null` = never.
 * @property lastSeenEpisode the normalized episode key of the newest release seen (`"1216"` for `"1216v2"`).
 * @property resolvedThrough the latest weekly occurrence already handled; `null` = 24 h before [trackingSince].
 */
@Entity(tableName = "bookmark")
data class BookmarkEntity(
    @PrimaryKey @ColumnInfo(name = "slug") val slug: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "cover_url") val coverUrl: String?,
    @ColumnInfo(name = "added_at") val addedAt: Long,
    @ColumnInfo(name = "tracking_since") val trackingSince: Long,
    @ColumnInfo(name = "airing_day") val airingDay: Int? = null,
    @ColumnInfo(name = "airing_minute") val airingMinute: Int? = null,
    @ColumnInfo(name = "airing_zone") val airingZone: String? = null,
    @ColumnInfo(name = "airing_title") val airingTitle: String? = null,
    @ColumnInfo(name = "airing_checked_at") val airingCheckedAt: Long? = null,
    @ColumnInfo(name = "last_seen_episode") val lastSeenEpisode: String? = null,
    @ColumnInfo(name = "last_seen_released_at") val lastSeenReleasedAt: Long? = null,
    @ColumnInfo(name = "resolved_through") val resolvedThrough: Long? = null,
)
