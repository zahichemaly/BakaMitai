package com.zc.bakamitai.feature.schedule.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowTestData.scheduled
import com.zc.bakamitai.common.domain.util.FixedTimeZoneProvider
import com.zc.bakamitai.feature.schedule.domain.model.WeeklyTemplate
import com.zc.bakamitai.feature.schedule.domain.repository.FakeScheduleRepository
import com.zc.bakamitai.feature.schedule.domain.repository.FakeScheduleRepository.Call
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class GetWeeklyTemplateUseCaseTest {

    private val repository = FakeScheduleRepository()
    private val tokyo = TimeZone.of("Asia/Tokyo")
    private val timeZoneProvider = FixedTimeZoneProvider(tokyo)
    private val useCase = GetWeeklyTemplateUseCase(repository, timeZoneProvider)

    @Test
    fun `returns the weekly schedule in the current zone`() = runTest {
        val days = mapOf(DayOfWeek.MONDAY to listOf(scheduled("frieren", LocalTime(23, 30))))
        repository.weekly = Result.success(days)

        assertEquals(Result.success(WeeklyTemplate(tokyo, days)), useCase())
        assertEquals(listOf<Call>(Call.Weekly(tokyo, forceRefresh = false)), repository.calls)
    }

    @Test
    fun `passes forceRefresh down and follows zone changes`() = runTest {
        timeZoneProvider.timeZone = TimeZone.UTC

        useCase(forceRefresh = true)

        assertEquals(listOf<Call>(Call.Weekly(TimeZone.UTC, forceRefresh = true)), repository.calls)
    }

    @Test
    fun `a failure is propagated`() = runTest {
        repository.weekly = Result.failure(DomainError.Network)

        assertEquals(DomainError.Network, useCase().exceptionOrNull())
    }
}
