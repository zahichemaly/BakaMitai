package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowTestData.scheduled
import com.zc.bakamitai.common.domain.util.FixedTimeZoneProvider
import com.zc.bakamitai.common.util.FixedClock
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.watch
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeBookmarkRepository
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseCheckScheduler
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseCheckScheduler.Call
import com.zc.bakamitai.feature.schedule.domain.repository.FakeScheduleRepository
import com.zc.bakamitai.feature.schedule.domain.usecase.GetWeeklyTemplateUseCase
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class RefreshAiringSlotsUseCaseTest {

    private val repository = FakeBookmarkRepository()
    private val schedule = FakeScheduleRepository()
    private val scheduler = FakeReleaseCheckScheduler()
    private val useCase = RefreshAiringSlotsUseCase(
        updateAiringSlots = UpdateAiringSlotsUseCase(
            repository = repository,
            getWeeklyTemplate = GetWeeklyTemplateUseCase(schedule, FixedTimeZoneProvider(TimeZone.UTC)),
            clock = FixedClock(Instant.parse("2026-11-19T10:00:00Z")),
        ),
        scheduler = scheduler,
    )

    @Test
    fun `a changed slot asks for a check`() = runTest {
        repository.watches = listOf(watch(airing = null, airingCheckedAt = null))
        schedule.weekly = Result.success(mapOf(DayOfWeek.MONDAY to listOf(scheduled("frieren", LocalTime(12, 0)))))

        assertEquals(Result.success(Unit), useCase(forceRefresh = false))
        assertEquals(listOf<Call>(Call.RequestCheck), scheduler.calls)
    }

    @Test
    fun `unchanged slots only make sure a check is scheduled`() = runTest {
        repository.watches = listOf(watch(airingCheckedAt = "2026-11-19T09:00:00Z"))

        assertEquals(Result.success(Unit), useCase(forceRefresh = false))
        assertEquals(listOf<Call>(Call.EnsureScheduled), scheduler.calls)
    }

    @Test
    fun `a failure is returned, and a check is still made sure of`() = runTest {
        repository.watches = listOf(watch(airingCheckedAt = null))
        schedule.weekly = Result.failure(DomainError.Network)

        assertEquals(DomainError.Network, useCase(forceRefresh = true).exceptionOrNull())
        assertEquals(listOf<Call>(Call.EnsureScheduled), scheduler.calls)
    }
}
