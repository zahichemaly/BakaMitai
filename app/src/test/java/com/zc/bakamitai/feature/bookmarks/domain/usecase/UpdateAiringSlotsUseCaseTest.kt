package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.common.domain.model.ShowTestData.scheduled
import com.zc.bakamitai.common.domain.util.FixedTimeZoneProvider
import com.zc.bakamitai.common.util.FixedClock
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.slot
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.watch
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeBookmarkRepository
import com.zc.bakamitai.feature.schedule.domain.repository.FakeScheduleRepository
import com.zc.bakamitai.feature.schedule.domain.repository.FakeScheduleRepository.Call
import com.zc.bakamitai.feature.schedule.domain.usecase.GetWeeklyTemplateUseCase
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class UpdateAiringSlotsUseCaseTest {

    private val now = Instant.parse("2026-11-19T10:00:00Z")
    private val tokyo = TimeZone.of("Asia/Tokyo")
    private val repository = FakeBookmarkRepository()
    private val schedule = FakeScheduleRepository()
    private val useCase = UpdateAiringSlotsUseCase(
        repository = repository,
        getWeeklyTemplate = GetWeeklyTemplateUseCase(schedule, FixedTimeZoneProvider(tokyo)),
        clock = FixedClock(now),
    )

    private val fresh = "2026-11-19T00:00:00Z"

    @Test
    fun `fresh slots need no request`() = runTest {
        repository.watches = listOf(watch(airingCheckedAt = fresh))

        assertEquals(Result.success(false), useCase(forceRefresh = false))
        assertEquals(emptyList(), schedule.calls)
        assertEquals(emptyList(), repository.airingUpdates)
    }

    @Test
    fun `no bookmarks need no request, even when forced`() = runTest {
        assertEquals(Result.success(false), useCase(forceRefresh = true))
        assertEquals(emptyList(), schedule.calls)
    }

    @Test
    fun `a missing slot is fetched with the schedule's title, and an absent show gets none`() = runTest {
        repository.watches = listOf(
            watch(slug = "kyouran", title = "Kyouran Reijou – Nia Liston", airing = null, airingCheckedAt = null),
            watch(slug = "finished", airing = null, airingCheckedAt = fresh),
        )
        schedule.weekly = Result.success(
            mapOf(
                DayOfWeek.MONDAY to listOf(
                    scheduled("kyouran", LocalTime(21, 55), title = "Kyouran Reijou Nia Liston"),
                ),
            ),
        )

        assertEquals(Result.success(true), useCase(forceRefresh = false))

        assertEquals(listOf<Call>(Call.Weekly(tokyo, forceRefresh = false)), schedule.calls)
        val (slots, checkedAt) = repository.airingUpdates.single()
        assertEquals(now, checkedAt)
        assertEquals(
            mapOf(
                ShowSlug("kyouran") to slot(DayOfWeek.MONDAY, LocalTime(21, 55), "Kyouran Reijou Nia Liston", tokyo),
                ShowSlug("finished") to null,
            ),
            slots,
        )
    }

    @Test
    fun `a stale slot is refreshed, and unchanged slots report no change`() = runTest {
        val monday = slot(DayOfWeek.MONDAY, LocalTime(21, 0), "frieren", tokyo)
        repository.watches = listOf(watch(slug = "frieren", airing = monday, airingCheckedAt = "2026-11-18T09:00:00Z"))
        schedule.weekly = Result.success(mapOf(DayOfWeek.MONDAY to listOf(scheduled("frieren", LocalTime(21, 0)))))

        assertEquals(Result.success(false), useCase(forceRefresh = false))
        assertEquals(1, repository.airingUpdates.size)
    }

    @Test
    fun `a show listed twice keeps its first slot, Monday to Sunday`() = runTest {
        repository.watches = listOf(watch(slug = "twice", airing = null, airingCheckedAt = null))
        schedule.weekly = Result.success(
            mapOf(
                DayOfWeek.SUNDAY to listOf(scheduled("twice", LocalTime(9, 0))),
                DayOfWeek.WEDNESDAY to listOf(scheduled("twice", LocalTime(18, 0))),
            ),
        )

        useCase(forceRefresh = false)

        assertEquals(
            slot(DayOfWeek.WEDNESDAY, LocalTime(18, 0), "twice", tokyo),
            repository.airingUpdates.single().first[ShowSlug("twice")],
        )
    }

    @Test
    fun `forcing passes forceRefresh down even when slots are fresh`() = runTest {
        repository.watches = listOf(watch(airingCheckedAt = fresh))

        useCase(forceRefresh = true)

        assertEquals(listOf<Call>(Call.Weekly(tokyo, forceRefresh = true)), schedule.calls)
    }

    @Test
    fun `a schedule failure stores nothing`() = runTest {
        repository.watches = listOf(watch(airingCheckedAt = null))
        schedule.weekly = Result.failure(DomainError.Network)

        assertEquals(DomainError.Network, useCase(forceRefresh = false).exceptionOrNull())
        assertEquals(emptyList(), repository.airingUpdates)
    }

    @Test
    fun `a read failure is propagated`() = runTest {
        repository.watchesFailure = DomainError.Unknown(IllegalStateException("disk"))

        assertEquals(repository.watchesFailure, useCase(forceRefresh = true).exceptionOrNull())
    }
}
