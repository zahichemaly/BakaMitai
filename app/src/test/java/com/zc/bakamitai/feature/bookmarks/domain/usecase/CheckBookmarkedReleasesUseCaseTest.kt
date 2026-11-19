package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.common.domain.model.ShowTestData.scheduled
import com.zc.bakamitai.common.domain.util.FixedTimeZoneProvider
import com.zc.bakamitai.common.util.FixedClock
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.MONDAY_NOON
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.mondayNoon
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.seen
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.slot
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.watch
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseCheckOutcome
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatchUpdate
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeBookmarkRepository
import com.zc.bakamitai.feature.home.domain.HomeTestData.release
import com.zc.bakamitai.feature.home.domain.model.Release
import com.zc.bakamitai.feature.home.domain.repository.FakeBroadcastRepository
import com.zc.bakamitai.feature.home.domain.usecase.GetNewestReleasesUseCase
import com.zc.bakamitai.feature.home.domain.usecase.SearchShowReleasesUseCase
import com.zc.bakamitai.feature.schedule.domain.repository.FakeScheduleRepository
import com.zc.bakamitai.feature.schedule.domain.usecase.GetWeeklyTemplateUseCase
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import com.zc.bakamitai.feature.home.domain.repository.FakeBroadcastRepository.Call as BroadcastCall
import com.zc.bakamitai.feature.schedule.domain.repository.FakeScheduleRepository.Call as ScheduleCall

/** One Monday 12:00 UTC slot awaited from E = 16 Nov 2026 12:00 (docs/5-bookmarks.md §5.3, §4.1 quirks). */
class CheckBookmarkedReleasesUseCaseTest {

    private val utc = TimeZone.UTC
    private val clock = FixedClock(MONDAY_NOON + 10.minutes)
    private val repository = FakeBookmarkRepository()
    private val schedule = FakeScheduleRepository()
    private val broadcasts = FakeBroadcastRepository()
    private val timeZoneProvider = FixedTimeZoneProvider(utc)
    private val useCase = CheckBookmarkedReleasesUseCase(
        repository = repository,
        updateAiringSlots = UpdateAiringSlotsUseCase(
            repository = repository,
            getWeeklyTemplate = GetWeeklyTemplateUseCase(schedule, timeZoneProvider),
            clock = clock,
        ),
        getNewestReleases = GetNewestReleasesUseCase(broadcasts),
        searchShowReleases = SearchShowReleasesUseCase(broadcasts),
        clock = clock,
        timeZoneProvider = timeZoneProvider,
    )

    private val lastWeek = "2026-11-09T12:00:00Z"
    private val nextMondayCheck = Instant.parse("2026-11-23T12:10:00Z")

    /** An older release of another show, so the newest releases reach back past the slot. */
    private val olderOther = release("other", "03", "2026-11-15T00:00:00Z")

    private fun newest(vararg releases: Release) {
        broadcasts.searches[""] = Result.success(releases.toList())
    }

    @Test
    fun `no bookmarks make no request and schedule nothing`() = runTest {
        assertEquals(ReleaseCheckOutcome(emptyList(), emptyList(), nextCheckAt = null), useCase(alertsEnabled = true))
        assertEquals(emptyList(), broadcasts.calls)
        assertEquals(emptyList(), schedule.calls)
    }

    @Test
    fun `a read failure retries in an hour`() = runTest {
        repository.watchesFailure = DomainError.Unknown(IllegalStateException("disk"))

        assertEquals(clock.instant + 1.hours, useCase(alertsEnabled = true).nextCheckAt)
        assertEquals(emptyList(), broadcasts.calls)
    }

    @Test
    fun `nothing awaited makes no request and schedules the next slot`() = runTest {
        clock.instant = MONDAY_NOON - 1.hours
        repository.watches = listOf(watch(resolvedThrough = lastWeek))

        val outcome = useCase(alertsEnabled = true)

        assertEquals(ReleaseCheckOutcome(emptyList(), emptyList(), MONDAY_NOON + 10.minutes), outcome)
        assertEquals(emptyList(), broadcasts.calls)
    }

    @Test
    fun `with alerts off nothing is requested and awaited slots are given up`() = runTest {
        repository.watches = listOf(watch(airingCheckedAt = null))

        val outcome = useCase(alertsEnabled = false)

        assertEquals(listOf(ReleaseWatchUpdate(ShowSlug("frieren"), null, MONDAY_NOON)), outcome.updates)
        assertEquals(emptyList(), outcome.alerts)
        assertEquals(nextMondayCheck, outcome.nextCheckAt)
        assertEquals(emptyList(), broadcasts.calls)
        assertEquals(emptyList(), schedule.calls)
    }

    @Test
    fun `one request finds the new episodes of every awaited show`() = runTest {
        clock.instant = MONDAY_NOON + 15.minutes
        repository.watches = listOf(
            watch(slug = "frieren", lastSeen = seen("07", "2026-11-09T11:03:00Z"), resolvedThrough = lastWeek),
            watch(
                slug = "dandadan",
                airing = slot(DayOfWeek.MONDAY, LocalTime(12, 5), "Dandadan"),
                lastSeen = seen("06", "2026-11-09T11:08:00Z"),
                resolvedThrough = "2026-11-09T12:05:00Z",
            ),
        )
        newest(
            release("dandadan", "07", "2026-11-16T11:08:00Z"),
            release("frieren", "08", "2026-11-16T11:03:00Z"),
            release("unrelated", "12", "2026-11-16T10:00:00Z"),
            olderOther,
        )

        val outcome = useCase(alertsEnabled = true)

        assertEquals(listOf<BroadcastCall>(BroadcastCall.Search("", utc)), broadcasts.calls)
        assertEquals(listOf("08", "07"), outcome.alerts.map { it.episode })
        assertEquals(
            listOf(
                ReleaseWatchUpdate(ShowSlug("frieren"), seen("08", "2026-11-16T11:03:00Z"), MONDAY_NOON),
                ReleaseWatchUpdate(
                    ShowSlug("dandadan"),
                    seen("07", "2026-11-16T11:08:00Z"),
                    Instant.parse("2026-11-16T12:05:00Z"),
                ),
            ),
            outcome.updates,
        )
        assertEquals(Instant.parse("2026-11-23T12:15:00Z"), outcome.nextCheckAt)
    }

    @Test
    fun `when the search fails the first latest page is the sweep`() = runTest {
        repository.watches = listOf(watch(lastSeen = seen("07", "2026-11-09T11:03:00Z"), resolvedThrough = lastWeek))
        broadcasts.searches[""] = Result.failure(DomainError.Network)
        broadcasts.latestPages[1] = Result.success(listOf(release("frieren", "08", "2026-11-16T11:03:00Z"), olderOther))

        val outcome = useCase(alertsEnabled = true)

        assertEquals(listOf(BroadcastCall.Search("", utc), BroadcastCall.Latest(1, utc)), broadcasts.calls)
        assertEquals("08", outcome.alerts.single().episode)
    }

    @Test
    fun `when both feeds fail nothing changes and the ladder goes on`() = runTest {
        repository.watches = listOf(watch(resolvedThrough = lastWeek))
        broadcasts.searches[""] = Result.failure(DomainError.Network)
        broadcasts.latestPages[1] = Result.failure(DomainError.Timeout)

        assertEquals(
            ReleaseCheckOutcome(emptyList(), emptyList(), MONDAY_NOON + 30.minutes),
            useCase(alertsEnabled = true),
        )
    }

    @Test
    fun `a show the newest releases don't reach back to is searched by its schedule title`() = runTest {
        clock.instant = MONDAY_NOON + 2.hours
        repository.watches = listOf(
            watch(
                slug = "kyouran-reijou",
                title = "Kyouran Reijou – Nia Liston",
                airing = mondayNoon(releaseTitle = "Kyouran Reijou Nia Liston"),
                resolvedThrough = lastWeek,
                lastSeen = seen("07", "2026-11-09T11:00:00Z"),
            ),
        )
        newest(release("other", "02", "2026-11-16T13:00:00Z"), release("other", "01", "2026-11-16T12:30:00Z"))
        broadcasts.searches["Kyouran Reijou Nia Liston"] = Result.success(
            listOf(
                release("kyouran-reijou-s2", "01", "2026-11-16T11:30:00Z"),
                release("kyouran-reijou", "08", "2026-11-16T11:00:00Z"),
            ),
        )

        val outcome = useCase(alertsEnabled = true)

        assertEquals(
            listOf<BroadcastCall>(
                BroadcastCall.Search("", utc),
                BroadcastCall.Search("Kyouran Reijou Nia Liston", utc),
            ),
            broadcasts.calls,
        )
        assertEquals("08", outcome.alerts.single().episode)
        assertEquals(ShowSlug("kyouran-reijou"), outcome.alerts.single().show.slug)
    }

    @Test
    fun `newest releases that reach back to the slot need no per-show search`() = runTest {
        clock.instant = MONDAY_NOON + 2.hours
        repository.watches = listOf(watch(resolvedThrough = lastWeek))
        newest(release("other", "02", "2026-11-16T13:00:00Z"), olderOther)

        val outcome = useCase(alertsEnabled = true)

        assertEquals(listOf<BroadcastCall>(BroadcastCall.Search("", utc)), broadcasts.calls)
        assertEquals(emptyList(), outcome.alerts)
        assertEquals(MONDAY_NOON + 4.hours, outcome.nextCheckAt)
    }

    @Test
    fun `per-show searches are capped at five and the rest continue in 15 minutes`() = runTest {
        clock.instant = MONDAY_NOON + 65.minutes
        repository.watches = (1..6).map {
            watch(
                slug = "s$it",
                airing = mondayNoon("Show $it"),
                resolvedThrough = lastWeek,
            )
        }
        newest(release("other", "01", "2026-11-16T13:00:00Z"))

        val outcome = useCase(alertsEnabled = true)

        assertEquals(
            listOf<BroadcastCall>(BroadcastCall.Search("", utc)) + (1..5).map { BroadcastCall.Search("Show $it", utc) },
            broadcasts.calls,
        )
        assertEquals(clock.instant + 15.minutes, outcome.nextCheckAt)
    }

    @Test
    fun `a failed per-show search never gives the slot up`() = runTest {
        clock.instant = MONDAY_NOON + 25.hours
        repository.watches = listOf(watch(resolvedThrough = lastWeek, airingCheckedAt = "2026-11-17T00:00:00Z"))
        newest(release("other", "01", "2026-11-17T12:00:00Z"))
        broadcasts.searches["Frieren"] = Result.failure(DomainError.Server)

        val outcome = useCase(alertsEnabled = true)

        assertEquals(emptyList(), outcome.updates)
        assertEquals(clock.instant + 1.hours, outcome.nextCheckAt)
    }

    @Test
    fun `a check that covers the slot a day later gives it up silently`() = runTest {
        clock.instant = MONDAY_NOON + 24.hours
        repository.watches = listOf(watch(resolvedThrough = lastWeek, airingCheckedAt = "2026-11-17T00:00:00Z"))
        newest(olderOther)

        val outcome = useCase(alertsEnabled = true)

        assertEquals(listOf(ReleaseWatchUpdate(ShowSlug("frieren"), null, MONDAY_NOON)), outcome.updates)
        assertEquals(emptyList(), outcome.alerts)
        assertEquals(nextMondayCheck, outcome.nextCheckAt)
    }

    @Test
    fun `a bookmark that isn't on the schedule is notified when a check sees its release`() = runTest {
        repository.watches = listOf(
            watch(resolvedThrough = lastWeek),
            watch(slug = "one-piece", airing = null, lastSeen = seen("1155", "2026-11-09T02:00:00Z")),
        )
        newest(release("one-piece", "Movie", "2026-11-16T09:00:00Z"), olderOther)

        val outcome = useCase(alertsEnabled = true)

        val alert = outcome.alerts.single()
        assertEquals(ShowSlug("one-piece"), alert.show.slug)
        assertEquals("Movie", alert.episode)
    }

    @Test
    fun `checks due within 15 minutes of each other share a run`() = runTest {
        clock.instant = MONDAY_NOON - 1.hours
        repository.watches = listOf(
            watch(slug = "a", resolvedThrough = "2026-11-10T00:00:00Z"),
            watch(
                slug = "b",
                airing = slot(DayOfWeek.MONDAY, LocalTime(12, 10)),
                resolvedThrough = "2026-11-10T00:00:00Z",
            ),
            watch(
                slug = "c",
                airing = slot(DayOfWeek.MONDAY, LocalTime(13, 0)),
                resolvedThrough = "2026-11-10T00:00:00Z",
            ),
        )

        assertEquals(MONDAY_NOON + 20.minutes, useCase(alertsEnabled = true).nextCheckAt)
    }

    @Test
    fun `stale slots are refreshed before the check`() = runTest {
        repository.watches = listOf(watch(airing = null, airingCheckedAt = null))
        schedule.weekly = Result.success(
            mapOf(DayOfWeek.MONDAY to listOf(scheduled("frieren", LocalTime(12, 0), title = "Frieren"))),
        )
        newest(release("frieren", "08", "2026-11-16T11:03:00Z"), olderOther)

        val outcome = useCase(alertsEnabled = true)

        assertEquals(listOf<ScheduleCall>(ScheduleCall.Weekly(utc, forceRefresh = false)), schedule.calls)
        assertEquals(listOf<BroadcastCall>(BroadcastCall.Search("", utc)), broadcasts.calls)
        assertEquals(1, repository.airingUpdates.size)
        assertEquals("08", outcome.alerts.single().episode)
    }

    @Test
    fun `a slot that was never fetched is retried in an hour when the schedule fails`() = runTest {
        repository.watches = listOf(watch(airing = null, airingCheckedAt = null))
        schedule.weekly = Result.failure(DomainError.Network)

        val outcome = useCase(alertsEnabled = true)

        assertEquals(clock.instant + 1.hours, outcome.nextCheckAt)
        assertEquals(emptyList(), broadcasts.calls)
        assertNull(outcome.alerts.firstOrNull())
    }
}
