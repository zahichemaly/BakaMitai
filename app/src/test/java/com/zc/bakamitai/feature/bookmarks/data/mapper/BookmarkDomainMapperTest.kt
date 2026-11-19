package com.zc.bakamitai.feature.bookmarks.data.mapper

import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkAiringRow
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkEntity
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkTrackingRow
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.bookmark
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.seen
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.slot
import com.zc.bakamitai.feature.bookmarks.domain.model.AiringSlot
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatchUpdate
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class BookmarkDomainMapperTest {

    private val tracking = Instant.parse("2026-11-19T08:00:00Z")
    private val sunday = slot(DayOfWeek.SUNDAY, LocalTime(9, 30), "One Piece", TimeZone.of("Asia/Tokyo"))

    @Test
    fun `a new bookmark has no slot and no tracking state`() {
        val bookmark = bookmark("frieren", "2026-11-10T00:00:00Z", title = "Sousou no Frieren")

        assertEquals(
            BookmarkEntity(
                slug = "frieren",
                title = "Sousou no Frieren",
                coverUrl = null,
                addedAt = Instant.parse("2026-11-10T00:00:00Z").toEpochMilliseconds(),
                trackingSince = tracking.toEpochMilliseconds(),
            ),
            bookmark.toEntity(trackingSince = tracking),
        )
    }

    @Test
    fun `a restored bookmark keeps its slot, marked as never fetched`() {
        val entity = bookmark("one-piece", "2026-11-10T00:00:00Z", airing = sunday).toEntity(tracking)

        assertEquals(7, entity.airingDay)
        assertEquals(9 * 60 + 30, entity.airingMinute)
        assertEquals("Asia/Tokyo", entity.airingZone)
        assertEquals("One Piece", entity.airingTitle)
        assertEquals(null, entity.airingCheckedAt)
    }

    @Test
    fun `a slot maps to its columns, and no slot clears them`() {
        val checkedAt = Instant.parse("2026-11-19T00:00:00Z")

        assertEquals(
            BookmarkAiringRow("one-piece", 7, 570, "Asia/Tokyo", "One Piece", checkedAt.toEpochMilliseconds()),
            sunday.toAiringRow(ShowSlug("one-piece"), checkedAt),
        )
        assertEquals(
            BookmarkAiringRow("one-piece", null, null, null, null, checkedAt.toEpochMilliseconds()),
            (null as AiringSlot?).toAiringRow(ShowSlug("one-piece"), checkedAt),
        )
    }

    @Test
    fun `a tracking update maps to its columns`() {
        val resolved = Instant.parse("2026-11-16T12:00:00Z")

        assertEquals(
            BookmarkTrackingRow(
                slug = "frieren",
                lastSeenEpisode = "08",
                lastSeenReleasedAt = Instant.parse("2026-11-16T11:00:00Z").toEpochMilliseconds(),
                resolvedThrough = resolved.toEpochMilliseconds(),
            ),
            ReleaseWatchUpdate(ShowSlug("frieren"), seen("08", "2026-11-16T11:00:00Z"), resolved).toTrackingRow(),
        )
        assertEquals(
            BookmarkTrackingRow("frieren", null, null, null),
            ReleaseWatchUpdate(ShowSlug("frieren"), null, null).toTrackingRow(),
        )
    }
}
