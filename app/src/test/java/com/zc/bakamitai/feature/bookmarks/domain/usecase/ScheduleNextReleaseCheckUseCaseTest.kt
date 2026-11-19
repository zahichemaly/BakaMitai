package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseCheckScheduler
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseCheckScheduler.Call
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ScheduleNextReleaseCheckUseCaseTest {

    @Test
    fun `passes the due time, or null, to the scheduler`() {
        val scheduler = FakeReleaseCheckScheduler()
        val useCase = ScheduleNextReleaseCheckUseCase(scheduler)
        val at = Instant.parse("2026-11-23T12:10:00Z")

        useCase(at)
        useCase(null)

        assertEquals(listOf<Call>(Call.ScheduleNext(at), Call.ScheduleNext(null)), scheduler.calls)
    }
}
