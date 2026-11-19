package com.zc.bakamitai.feature.bookmarks.data.repository

import app.cash.turbine.test
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkEntity
import com.zc.bakamitai.feature.bookmarks.data.local.FakeBookmarkDao
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.bookmark
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.mondayNoon
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.seen
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkToggle
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatchUpdate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Instant

class BookmarkRepositoryImplTest {

    private val dao = FakeBookmarkDao()
    private val repository = BookmarkRepositoryImpl(dao)
    private val frieren = bookmark("frieren", "2026-11-10T00:00:00Z", title = "Sousou no Frieren")
    private val dandadan = bookmark("dandadan", "2026-11-12T00:00:00Z", title = "Dandadan")

    @Test
    fun `toggle adds, tracking from the date added, then removes`() = runTest {
        assertEquals(
            Result.success(BookmarkToggle(isBookmarked = true, isFirstBookmark = true)),
            repository.toggle(frieren),
        )
        assertEquals(frieren.addedAt.toEpochMilliseconds(), dao.rows.value.getValue("frieren").trackingSince)

        assertEquals(
            Result.success(BookmarkToggle(isBookmarked = false, isFirstBookmark = false)),
            repository.toggle(frieren),
        )
        assertEquals(emptyMap(), dao.rows.value)
    }

    @Test
    fun `only the add from zero to one is the first bookmark`() = runTest {
        assertEquals(true, repository.toggle(frieren).getOrThrow().isFirstBookmark)
        assertEquals(false, repository.toggle(dandadan).getOrThrow().isFirstBookmark)
    }

    @Test
    fun `observeBookmarks is newest first and drops unusable rows`() = runTest {
        repository.toggle(frieren)
        repository.toggle(dandadan)
        dao.insert(BookmarkEntity(slug = "blank", title = " ", coverUrl = null, addedAt = 0, trackingSince = 0))

        repository.observeBookmarks().test {
            assertEquals(listOf(dandadan, frieren), awaitItem())
        }
    }

    @Test
    fun `remove returns the removed bookmark, or null`() = runTest {
        repository.toggle(frieren)

        assertEquals(Result.success(frieren), repository.remove(ShowSlug("frieren")))
        assertEquals(Result.success(null), repository.remove(ShowSlug("frieren")))
    }

    @Test
    fun `restore keeps the date and slot, tracks from the given time and refreshes the slot`() = runTest {
        val withSlot = frieren.copy(airing = mondayNoon())
        val now = Instant.parse("2026-11-19T08:00:00Z")

        assertEquals(Result.success(Unit), repository.restore(withSlot, trackingSince = now))

        val row = dao.rows.value.getValue("frieren")
        assertEquals(frieren.addedAt.toEpochMilliseconds(), row.addedAt)
        assertEquals(now.toEpochMilliseconds(), row.trackingSince)
        assertNull(row.airingCheckedAt)
        assertNull(row.lastSeenEpisode)
        assertEquals(listOf(withSlot), repository.observeBookmarks().first())
    }

    @Test
    fun `slots and tracking are stored, and slugs no longer saved are ignored`() = runTest {
        repository.toggle(frieren)
        val checkedAt = Instant.parse("2026-11-19T08:00:00Z")

        repository.updateAiringSlots(
            mapOf(ShowSlug("frieren") to mondayNoon(), ShowSlug("gone") to null),
            checkedAt = checkedAt,
        )
        repository.saveReleaseWatches(
            listOf(
                ReleaseWatchUpdate(
                    ShowSlug("frieren"),
                    seen("08", "2026-11-16T11:00:00Z"),
                    Instant.parse("2026-11-16T12:00:00Z"),
                ),
            ),
        )

        val watch = repository.getReleaseWatches().getOrThrow().single()
        assertEquals(mondayNoon(), watch.airing)
        assertEquals(checkedAt, watch.airingCheckedAt)
        assertEquals(seen("08", "2026-11-16T11:00:00Z"), watch.lastSeen)
        assertEquals(Instant.parse("2026-11-16T12:00:00Z"), watch.resolvedThrough)
        assertEquals(frieren.addedAt, watch.trackingSince)
    }

    @Test
    fun `saving no updates writes nothing`() = runTest {
        assertEquals(Result.success(Unit), repository.saveReleaseWatches(emptyList()))
        assertEquals(0, dao.trackingWrites)
    }

    @Test
    fun `cancellation isn't caught`() = runTest {
        dao.failure = CancellationException("cancelled")

        assertFailsWith<CancellationException> { repository.getReleaseWatches() }
    }
}
