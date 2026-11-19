package com.zc.bakamitai.feature.bookmarks.domain.usecase

import app.cash.turbine.test
import com.zc.bakamitai.common.domain.util.FixedTimeZoneProvider
import com.zc.bakamitai.common.util.FixedClock
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.bookmark
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.slot
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkSort
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkedShow
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeBookmarkRepository
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ObserveBookmarksUseCaseTest {

    // Thursday 19 Nov 2026, 10:00 UTC = 19:00 in Tokyo.
    private val clock = FixedClock(Instant.parse("2026-11-19T10:00:00Z"))
    private val tokyo = TimeZone.of("Asia/Tokyo")
    private val repository = FakeBookmarkRepository()
    private val useCase = ObserveBookmarksUseCase(repository, clock, FixedTimeZoneProvider(tokyo))

    /** Thursday 21:30 Tokyo: later today. */
    private val tonight = slot(DayOfWeek.THURSDAY, LocalTime(21, 30), timeZone = tokyo)

    /** Monday 12:00 UTC = Monday 21:00 Tokyo. */
    private val monday = slot(DayOfWeek.MONDAY, LocalTime(12, 0))

    private val frieren = bookmark("frieren", "2026-11-10T00:00:00Z", title = "Sousou no Frieren", airing = tonight)
    private val dandadan = bookmark("dandadan", "2026-11-18T00:00:00Z", title = "Dandadan", airing = monday)
    private val bleach = bookmark("bleach", "2026-11-12T00:00:00Z", title = "bleach", airing = null)

    private fun List<BookmarkedShow>.slugs() = map { it.bookmark.show.slug.value }

    @Test
    fun `recently added is newest first`() = runTest {
        repository.bookmarks.value = listOf(frieren, dandadan, bleach)

        useCase(BookmarkSort.RECENTLY_ADDED).test {
            assertEquals(listOf("dandadan", "bleach", "frieren"), awaitItem().slugs())
        }
    }

    @Test
    fun `title order ignores case`() = runTest {
        repository.bookmarks.value = listOf(frieren, dandadan, bleach)

        useCase(BookmarkSort.TITLE).test {
            assertEquals(listOf("bleach", "dandadan", "frieren"), awaitItem().slugs())
        }
    }

    @Test
    fun `release time is soonest first, with shows not airing last by title`() = runTest {
        val zeta = bookmark("zeta", "2026-11-01T00:00:00Z", title = "Zeta", airing = null)
        repository.bookmarks.value = listOf(zeta, dandadan, bleach, frieren)

        useCase(BookmarkSort.NEXT_AIRING).test {
            assertEquals(listOf("frieren", "dandadan", "bleach", "zeta"), awaitItem().slugs())
        }
    }

    @Test
    fun `next airing and today are in the device's zone`() = runTest {
        repository.bookmarks.value = listOf(frieren, dandadan, bleach)

        useCase(BookmarkSort.RECENTLY_ADDED).test {
            val shows = awaitItem().associateBy { it.bookmark.show.slug.value }
            assertEquals(LocalDate(2026, 11, 10), shows.getValue("frieren").addedOn)
            assertEquals(LocalDateTime(2026, 11, 19, 21, 30), shows.getValue("frieren").nextAiring)
            assertTrue(shows.getValue("frieren").airsToday)
            assertEquals(LocalDateTime(2026, 11, 23, 21, 0), shows.getValue("dandadan").nextAiring)
            assertFalse(shows.getValue("dandadan").airsToday)
            assertNull(shows.getValue("bleach").nextAiring)
            assertFalse(shows.getValue("bleach").airsToday)
        }
    }

    @Test
    fun `labels move on once the slot has passed, and equal lists aren't re-emitted`() = runTest {
        repository.bookmarks.value = listOf(frieren)

        useCase(BookmarkSort.RECENTLY_ADDED).test {
            assertTrue(awaitItem().single().airsToday)

            advanceTimeBy(1.minutes + 1.minutes)
            expectNoEvents()

            clock.instant = Instant.parse("2026-11-19T12:31:00Z") // 21:31 in Tokyo
            advanceTimeBy(1.minutes)
            val moved = awaitItem().single()
            assertEquals(LocalDateTime(2026, 11, 26, 21, 30), moved.nextAiring)
            assertFalse(moved.airsToday)
        }
    }

    @Test
    fun `a bookmark change is emitted right away`() = runTest {
        useCase(BookmarkSort.RECENTLY_ADDED).test {
            assertEquals(emptyList(), awaitItem())

            repository.bookmarks.value = listOf(frieren)
            assertEquals(listOf("frieren"), awaitItem().slugs())
        }
    }
}
