package com.zc.bakamitai.feature.home.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.util.FixedTimeZoneProvider
import com.zc.bakamitai.common.util.FixedClock
import com.zc.bakamitai.feature.home.domain.HomeTestData.release
import com.zc.bakamitai.feature.home.domain.HomeTestData.scheduled
import com.zc.bakamitai.feature.home.domain.HomeTestData.show
import com.zc.bakamitai.feature.home.domain.model.TodayBroadcasts
import com.zc.bakamitai.feature.home.domain.model.UpcomingBroadcast
import com.zc.bakamitai.feature.home.domain.repository.FakeBroadcastRepository
import com.zc.bakamitai.feature.home.domain.repository.FakeBroadcastRepository.Call
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class GetTodayBroadcastsUseCaseTest {

    private val newYork = TimeZone.of("America/New_York")
    private val repository = FakeBroadcastRepository()
    private val timeZoneProvider = FixedTimeZoneProvider(newYork)

    /** 15:30 in New York on Wed 18 Nov 2026. */
    private val clock = FixedClock(Instant.parse("2026-11-18T20:30:00Z"))

    private val useCase = GetTodayBroadcastsUseCase(
        repository = repository,
        findLatestEpisodeNumber = FindLatestEpisodeNumberUseCase(repository),
        clock = clock,
        timeZoneProvider = timeZoneProvider,
    )

    private suspend fun load(): TodayBroadcasts = useCase().getOrThrow()

    @Test
    fun `returns the date, zone and offset of today in the device zone`() = runTest {
        val broadcasts = load()

        assertEquals(LocalDate(2026, 11, 18), broadcasts.date)
        assertEquals(newYork, broadcasts.timeZone)
        assertEquals(UtcOffset(hours = -5), broadcasts.utcOffset)
    }

    @Test
    fun `every call is made for the device zone`() = runTest {
        load()

        assertTrue(repository.calls.isNotEmpty())
        assertTrue(repository.calls.all { it.timeZone == newYork })
    }

    @Test
    fun `upcoming lists only scheduled shows that haven't aired, by time then title`() = runTest {
        repository.schedule = Result.success(
            listOf(
                scheduled("evening", LocalTime(19, 0)),
                scheduled("aired", LocalTime(8, 0), aired = true),
                scheduled("noon-b", LocalTime(12, 0), title = "B"),
                scheduled("noon-a", LocalTime(12, 0), title = "A"),
            ),
        )

        val upcoming = load().upcoming

        assertEquals(listOf("noon-a", "noon-b", "evening"), upcoming.map { it.show.slug.value })
        assertEquals(LocalTime(12, 0), upcoming.first().time)
    }

    @Test
    fun `an upcoming show already released today is removed from upcoming`() = runTest {
        repository.schedule = Result.success(
            listOf(scheduled("tensei", LocalTime(10, 30)), scheduled("later", LocalTime(18, 0))),
        )
        repository.searches[""] = Result.success(listOf(release("tensei", "08", "2026-11-18T14:33:47Z")))

        val broadcasts = load()

        assertEquals(listOf("later"), broadcasts.upcoming.map { it.show.slug.value })
        assertEquals(listOf("tensei"), broadcasts.released.map { it.show.slug.value })
    }

    @Test
    fun `released keeps only releases dated today in the device zone`() = runTest {
        // 2026-11-18T16:00Z is 11:00 on the 18th in New York and 01:00 on the 19th in Tokyo.
        clock.instant = Instant.parse("2026-11-18T16:00:00Z")
        repository.searches[""] = Result.success(
            listOf(
                release("before-midnight-tokyo", "01", "2026-11-18T14:30:00Z"),
                release("after-midnight-tokyo", "01", "2026-11-18T15:30:00Z"),
                release("yesterday-new-york", "01", "2026-11-18T04:30:00Z"),
            ),
        )

        val inNewYork = load().released.map { it.show.slug.value }
        timeZoneProvider.timeZone = TimeZone.of("Asia/Tokyo")
        val inTokyo = load().released.map { it.show.slug.value }

        assertEquals(listOf("before-midnight-tokyo", "after-midnight-tokyo"), inNewYork)
        assertEquals(listOf("after-midnight-tokyo"), inTokyo)
    }

    @Test
    fun `released is ascending and lists each episode of a show separately`() = runTest {
        repository.latestPages[1] = Result.success(
            listOf(
                release("gensou-suikoden", "07", "2026-11-18T14:04:31Z"),
                release("gensou-suikoden", "02", "2026-11-18T13:35:55Z"),
                release("chii-fuyo", "07", "2026-11-18T14:32:06Z"),
            ),
        )

        val released = load().released

        assertEquals(
            listOf("gensou-suikoden" to "02", "gensou-suikoden" to "07", "chii-fuyo" to "07"),
            released.map { it.show.slug.value to it.episode },
        )
    }

    @Test
    fun `latest and newest releases are merged without duplicates`() = runTest {
        repository.latestPages[1] = Result.success(
            listOf(
                release("kikansha", "19", "2026-11-18T16:17:09Z"),
                release("dandivine", "07", "2026-11-18T16:02:12Z"),
            ),
        )
        repository.searches[""] = Result.success(
            listOf(
                release("kikansha", "19", "2026-11-18T16:17:09Z"),
                release("tensei", "08", "2026-11-18T14:33:47Z"),
            ),
        )

        val released = load().released

        assertEquals(listOf("tensei", "dandivine", "kikansha"), released.map { it.show.slug.value })
    }

    @Test
    fun `when latest lags, today's releases come from the newest search`() = runTest {
        repository.latestPages[1] = Result.success(
            listOf(
                release("psyren", "06", "2026-11-16T13:32:02Z"),
                release("kanata-kara", "07", "2026-11-15T15:03:53Z"),
            ),
        )
        repository.searches[""] = Result.success(
            listOf(
                release("kikansha", "19", "2026-11-18T16:17:09Z"),
                release("sora", "20", "2026-11-17T17:38:52Z"),
            ),
        )

        val released = load().released

        assertEquals(listOf("kikansha"), released.map { it.show.slug.value })
        assertFalse(Call.Latest(2, newYork) in repository.calls)
    }

    @Test
    fun `the second latest page is requested when the whole first page is from today`() = runTest {
        repository.latestPages[1] = Result.success(listOf(release("kikansha", "19", "2026-11-18T16:17:09Z")))
        repository.latestPages[2] = Result.success(listOf(release("sasaki", "07", "2026-11-18T12:03:04Z")))

        val released = load().released

        assertTrue(Call.Latest(2, newYork) in repository.calls)
        assertEquals(listOf("sasaki", "kikansha"), released.map { it.show.slug.value })
    }

    @Test
    fun `an empty first page doesn't request the second`() = runTest {
        load()

        assertFalse(Call.Latest(2, newYork) in repository.calls)
    }

    @Test
    fun `a limit_reached or failed second page is ignored`() = runTest {
        repository.latestPages[1] = Result.success(listOf(release("kikansha", "19", "2026-11-18T16:17:09Z")))

        repository.latestPages[2] = Result.success(emptyList())
        assertEquals(listOf("kikansha"), load().released.map { it.show.slug.value })

        repository.latestPages[2] = Result.failure(DomainError.Server)
        assertEquals(listOf("kikansha"), load().released.map { it.show.slug.value })
    }

    @Test
    fun `a schedule failure fails the result`() = runTest {
        repository.schedule = Result.failure(DomainError.Network)

        assertEquals(DomainError.Network, useCase().exceptionOrNull())
    }

    @Test
    fun `a first latest page failure fails the result`() = runTest {
        repository.latestPages[1] = Result.failure(DomainError.InvalidData)

        assertEquals(DomainError.InvalidData, useCase().exceptionOrNull())
    }

    @Test
    fun `a newest search failure still succeeds`() = runTest {
        repository.latestPages[1] = Result.success(listOf(release("kikansha", "19", "2026-11-18T16:17:09Z")))
        repository.searches[""] = Result.failure(DomainError.Timeout)

        assertEquals(listOf("kikansha"), load().released.map { it.show.slug.value })
    }

    @Test
    fun `episode numbers come from known releases, else from a search, else are unknown`() = runTest {
        repository.schedule = Result.success(
            listOf(
                scheduled("weekly", LocalTime(16, 0), title = "Weekly"),
                scheduled("older", LocalTime(17, 0), title = "Older"),
                scheduled("unknown", LocalTime(18, 0), title = "Unknown"),
            ),
        )
        repository.latestPages[1] = Result.success(listOf(release("weekly", "07", "2026-11-11T21:00:00Z")))
        repository.searches["Older"] = Result.success(listOf(release("older", "11", "2026-10-28T22:00:00Z")))
        repository.searches["Unknown"] = Result.failure(DomainError.Network)

        val upcoming = load().upcoming

        assertEquals(
            listOf(
                UpcomingBroadcast(show("weekly", "Weekly"), LocalTime(16, 0), episodeNumber = 8),
                UpcomingBroadcast(show("older", "Older"), LocalTime(17, 0), episodeNumber = 12),
                UpcomingBroadcast(show("unknown", "Unknown"), LocalTime(18, 0), episodeNumber = null),
            ),
            upcoming,
        )
        assertFalse(Call.Search("Weekly", newYork) in repository.calls)
    }

    @Test
    fun `episode lookups run concurrently`() = runTest {
        repository.schedule = Result.success(
            listOf(
                scheduled("a", LocalTime(16, 0), title = "A"),
                scheduled("b", LocalTime(17, 0), title = "B"),
                scheduled("c", LocalTime(18, 0), title = "C"),
            ),
        )
        val gate = CompletableDeferred<Unit>()
        repository.showSearchGate = gate

        val result = async { useCase() }
        runCurrentAndAssertAllLookupsStarted()
        gate.complete(Unit)

        assertEquals(3, result.await().getOrThrow().upcoming.size)
    }

    private fun TestScope.runCurrentAndAssertAllLookupsStarted() {
        runCurrent()
        val started = repository.calls.filterIsInstance<Call.Search>().map { it.query }.filter { it.isNotEmpty() }
        assertEquals(setOf("A", "B", "C"), started.toSet())
        assertTrue(repository.completedSearches.none { it.isNotEmpty() })
    }
}
