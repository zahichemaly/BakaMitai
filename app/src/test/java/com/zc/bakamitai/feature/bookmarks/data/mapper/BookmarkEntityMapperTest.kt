package com.zc.bakamitai.feature.bookmarks.data.mapper

import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkEntity
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.seen
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.slot
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class BookmarkEntityMapperTest {

    private val addedAt = Instant.parse("2026-11-10T00:00:00Z")
    private val tracking = Instant.parse("2026-11-12T00:00:00Z")

    private val entity = BookmarkEntity(
        slug = "frieren",
        title = "Sousou no Frieren",
        coverUrl = "https://subsplease.org/a.jpg",
        addedAt = addedAt.toEpochMilliseconds(),
        trackingSince = tracking.toEpochMilliseconds(),
        airingDay = 5,
        airingMinute = 23 * 60 + 30,
        airingZone = "Asia/Tokyo",
        airingTitle = "Sousou no Frieren S2",
        airingCheckedAt = Instant.parse("2026-11-19T00:00:00Z").toEpochMilliseconds(),
        lastSeenEpisode = "08",
        lastSeenReleasedAt = Instant.parse("2026-11-14T14:00:00Z").toEpochMilliseconds(),
        resolvedThrough = Instant.parse("2026-11-14T14:30:00Z").toEpochMilliseconds(),
    )

    private val friday = slot(DayOfWeek.FRIDAY, LocalTime(23, 30), "Sousou no Frieren S2", TimeZone.of("Asia/Tokyo"))
    private val frieren = show("frieren", "Sousou no Frieren", "https://subsplease.org/a.jpg")

    @Test
    fun `a row maps to its bookmark with its slot`() {
        assertEquals(Bookmark(frieren, addedAt, friday), entity.toBookmark())
    }

    @Test
    fun `a row maps to its release tracking`() {
        assertEquals(
            ReleaseWatch(
                show = frieren,
                trackingSince = tracking,
                airing = friday,
                airingCheckedAt = Instant.parse("2026-11-19T00:00:00Z"),
                lastSeen = seen("08", "2026-11-14T14:00:00Z"),
                resolvedThrough = Instant.parse("2026-11-14T14:30:00Z"),
            ),
            entity.toReleaseWatch(),
        )
    }

    @Test
    fun `a slot needs every airing column, valid`() {
        assertNull(entity.copy(airingDay = null).toBookmark()?.airing)
        assertNull(entity.copy(airingDay = 8).toBookmark()?.airing)
        assertNull(entity.copy(airingDay = 0).toBookmark()?.airing)
        assertNull(entity.copy(airingMinute = 24 * 60).toBookmark()?.airing)
        assertNull(entity.copy(airingMinute = -1).toBookmark()?.airing)
        assertNull(entity.copy(airingZone = "Mars/Olympus").toBookmark()?.airing)
        assertNull(entity.copy(airingTitle = " ").toBookmark()?.airing)
        assertEquals(LocalTime(0, 0), entity.copy(airingMinute = 0).toBookmark()?.airing?.time)
    }

    @Test
    fun `missing tracking columns map to null`() {
        val watch = entity.copy(
            airingCheckedAt = null,
            lastSeenReleasedAt = null,
            resolvedThrough = null,
        ).toReleaseWatch()

        assertNull(watch?.airingCheckedAt)
        assertNull(watch?.lastSeen)
        assertNull(watch?.resolvedThrough)
        assertNull(entity.copy(lastSeenEpisode = "").toReleaseWatch()?.lastSeen)
    }

    @Test
    fun `a row without a slug or a title is unusable`() {
        assertNull(entity.copy(slug = "").toBookmark())
        assertNull(entity.copy(title = " ").toBookmark())
        assertNull(entity.copy(slug = " ").toReleaseWatch())
    }
}
