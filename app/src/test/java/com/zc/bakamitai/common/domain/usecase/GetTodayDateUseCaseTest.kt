package com.zc.bakamitai.common.domain.usecase

import com.zc.bakamitai.common.domain.util.FixedTimeZoneProvider
import com.zc.bakamitai.common.util.FixedClock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class GetTodayDateUseCaseTest {

    private val clock = FixedClock(Instant.parse("2026-11-18T20:00:00Z"))
    private val timeZoneProvider = FixedTimeZoneProvider(TimeZone.of("America/New_York"))
    private val useCase = GetTodayDateUseCase(clock, timeZoneProvider)

    @Test
    fun `the date follows the provider's zone for the same instant`() {
        assertEquals(LocalDate(2026, 11, 18), useCase())

        timeZoneProvider.timeZone = TimeZone.of("Asia/Tokyo")

        assertEquals(LocalDate(2026, 11, 19), useCase())
    }

    @Test
    fun `the date follows the clock`() {
        clock.instant = Instant.parse("2026-11-19T05:00:00Z")

        assertEquals(LocalDate(2026, 11, 19), useCase())
    }
}
