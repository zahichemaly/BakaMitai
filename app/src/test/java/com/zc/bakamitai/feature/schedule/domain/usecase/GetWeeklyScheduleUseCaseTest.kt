package com.zc.bakamitai.feature.schedule.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowTestData.scheduled
import com.zc.bakamitai.common.domain.util.FixedTimeZoneProvider
import com.zc.bakamitai.common.util.FixedClock
import com.zc.bakamitai.feature.schedule.domain.repository.FakeScheduleRepository
import com.zc.bakamitai.feature.schedule.domain.repository.FakeScheduleRepository.Call
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class GetWeeklyScheduleUseCaseTest {

    private val newYork = TimeZone.of("America/New_York")
    private val tokyo = TimeZone.of("Asia/Tokyo")

    /** Thursday 2026-11-19, 12:00 in New York. */
    private val clock = FixedClock(Instant.parse("2026-11-19T17:00:00Z"))
    private val timeZoneProvider = FixedTimeZoneProvider(newYork)
    private val repository = FakeScheduleRepository()
    private val useCase = GetWeeklyScheduleUseCase(repository, clock, timeZoneProvider)

    private fun dates(first: LocalDate) = List(7) { LocalDate.fromEpochDays(first.toEpochDays() + it) }

    @Test
    fun `a Monday week start gives Monday to Sunday of the current week`() = runTest {
        val week = useCase(weekStart = DayOfWeek.MONDAY).getOrThrow()

        assertEquals(LocalDate(2026, 11, 19), week.today)
        assertEquals(dates(LocalDate(2026, 11, 16)), week.days.map { it.date })
        assertEquals(DayOfWeek.MONDAY, week.days.first().dayOfWeek)
        assertEquals(DayOfWeek.SUNDAY, week.days.last().dayOfWeek)
    }

    @Test
    fun `a Sunday week start gives Sunday to Saturday`() = runTest {
        val week = useCase(weekStart = DayOfWeek.SUNDAY).getOrThrow()

        assertEquals(dates(LocalDate(2026, 11, 15)), week.days.map { it.date })
    }

    @Test
    fun `today on the first day of the week comes first`() = runTest {
        clock.instant = Instant.parse("2026-11-16T17:00:00Z")

        val week = useCase(DayOfWeek.MONDAY).getOrThrow()

        assertEquals(dates(LocalDate(2026, 11, 16)), week.days.map { it.date })
        assertEquals(week.today, week.days.first().date)
    }

    @Test
    fun `today on the last day of the week comes last`() = runTest {
        clock.instant = Instant.parse("2026-11-22T17:00:00Z")

        val week = useCase(DayOfWeek.MONDAY).getOrThrow()

        assertEquals(dates(LocalDate(2026, 11, 16)), week.days.map { it.date })
        assertEquals(week.today, week.days.last().date)
    }

    @Test
    fun `the device zone decides today, the week and the offset`() = runTest {
        clock.instant = Instant.parse("2026-11-19T02:30:00Z")

        val inNewYork = useCase(DayOfWeek.MONDAY).getOrThrow()
        timeZoneProvider.timeZone = tokyo
        val inTokyo = useCase(DayOfWeek.MONDAY).getOrThrow()

        assertEquals(LocalDate(2026, 11, 18), inNewYork.today)
        assertEquals(UtcOffset(-5), inNewYork.utcOffset)
        assertEquals(newYork, inNewYork.timeZone)
        assertEquals(LocalDate(2026, 11, 19), inTokyo.today)
        assertEquals(UtcOffset(9), inTokyo.utcOffset)
        assertEquals(tokyo, inTokyo.timeZone)
        assertEquals(
            listOf(Call.Weekly(newYork, false), Call.Weekly(tokyo, false)),
            repository.calls.filterIsInstance<Call.Weekly>(),
        )
    }

    @Test
    fun `shows are sorted by time then title, and missing days are empty`() = runTest {
        repository.weekly = Result.success(
            mapOf(
                DayOfWeek.WEDNESDAY to listOf(
                    scheduled("c", LocalTime(10, 30), aired = null, title = "C"),
                    scheduled("b", LocalTime(8, 0), aired = null, title = "B"),
                    scheduled("a", LocalTime(8, 0), aired = null, title = "A"),
                ),
            ),
        )

        val week = useCase(DayOfWeek.MONDAY).getOrThrow()

        assertEquals(
            listOf("a", "b", "c"),
            week.days.single { it.dayOfWeek == DayOfWeek.WEDNESDAY }.shows.map { it.show.slug.value },
        )
        assertTrue(week.days.filter { it.dayOfWeek != DayOfWeek.WEDNESDAY }.all { it.shows.isEmpty() })
    }

    @Test
    fun `today's shows get aired from today's schedule by slug, other days have none`() = runTest {
        repository.weekly = Result.success(
            mapOf(
                DayOfWeek.THURSDAY to listOf(
                    scheduled("aired", LocalTime(8, 0), aired = null),
                    scheduled("upcoming", LocalTime(12, 0), aired = null),
                    scheduled("unknown", LocalTime(13, 0), aired = null),
                ),
                DayOfWeek.FRIDAY to listOf(scheduled("friday", LocalTime(9, 0), aired = true)),
            ),
        )
        repository.today = Result.success(
            listOf(
                scheduled("aired", LocalTime(8, 0), aired = true),
                scheduled("upcoming", LocalTime(12, 0), aired = false),
            ),
        )

        val week = useCase(DayOfWeek.MONDAY).getOrThrow()

        val thursday = week.days.single { it.date == week.today }
        assertEquals(listOf(true, false, null), thursday.shows.map { it.aired })
        assertTrue(thursday.hasStatuses)
        val friday = week.days.single { it.dayOfWeek == DayOfWeek.FRIDAY }
        assertEquals(listOf(null), friday.shows.map { it.aired })
        assertTrue(week.days.filter { it.date != week.today }.none { it.hasStatuses })
    }

    @Test
    fun `a failed today's schedule is a success without statuses`() = runTest {
        repository.weekly = Result.success(
            mapOf(DayOfWeek.THURSDAY to listOf(scheduled("a", LocalTime(8, 0), aired = null))),
        )
        repository.today = Result.failure(DomainError.Network)

        val week = useCase(DayOfWeek.MONDAY).getOrThrow()

        assertTrue(week.days.flatMap { it.shows }.all { it.aired == null })
        assertEquals(1, week.days.single { it.date == week.today }.shows.size)
    }

    @Test
    fun `a failed weekly schedule fails with its error`() = runTest {
        repository.weekly = Result.failure(DomainError.Server)

        assertEquals(DomainError.Server, useCase(DayOfWeek.MONDAY).exceptionOrNull())
    }

    @Test
    fun `forceRefresh and the zone are passed on`() = runTest {
        useCase(DayOfWeek.MONDAY, forceRefresh = true)

        assertEquals(setOf(Call.Weekly(newYork, forceRefresh = true), Call.Today(newYork)), repository.calls.toSet())
    }

    @Test
    fun `both requests start before either completes`() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate

        val result = async { useCase(DayOfWeek.MONDAY) }
        runCurrent()

        assertEquals(setOf(Call.Weekly(newYork, forceRefresh = false), Call.Today(newYork)), repository.calls.toSet())
        assertTrue(repository.completed.isEmpty())
        gate.complete(Unit)
        assertTrue(result.await().isSuccess)
    }
}
