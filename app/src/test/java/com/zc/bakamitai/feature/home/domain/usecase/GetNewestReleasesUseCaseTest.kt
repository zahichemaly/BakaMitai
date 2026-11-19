package com.zc.bakamitai.feature.home.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.feature.home.domain.HomeTestData.release
import com.zc.bakamitai.feature.home.domain.repository.FakeBroadcastRepository
import com.zc.bakamitai.feature.home.domain.repository.FakeBroadcastRepository.Call
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class GetNewestReleasesUseCaseTest {

    private val repository = FakeBroadcastRepository()
    private val useCase = GetNewestReleasesUseCase(repository)
    private val utc = TimeZone.UTC

    @Test
    fun `returns the empty search in one request`() = runTest {
        val newest = listOf(release("frieren", "08", "2026-11-19T09:00:00Z"))
        repository.searches[""] = Result.success(newest)

        assertEquals(Result.success(newest), useCase(utc))
        assertEquals(listOf<Call>(Call.Search("", utc)), repository.calls)
    }

    @Test
    fun `falls back to the first latest page when the search fails`() = runTest {
        val latest = listOf(release("dandadan", "07", "2026-11-18T09:00:00Z"))
        repository.searches[""] = Result.failure(DomainError.Network)
        repository.latestPages[1] = Result.success(latest)

        assertEquals(Result.success(latest), useCase(utc))
        assertEquals(listOf(Call.Search("", utc), Call.Latest(1, utc)), repository.calls)
    }

    @Test
    fun `fails when both feeds fail`() = runTest {
        repository.searches[""] = Result.failure(DomainError.Network)
        repository.latestPages[1] = Result.failure(DomainError.Server)

        assertEquals(DomainError.Server, useCase(utc).exceptionOrNull())
    }
}
