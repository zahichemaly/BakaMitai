package com.zc.bakamitai.feature.home.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.home.domain.HomeTestData.release
import com.zc.bakamitai.feature.home.domain.repository.FakeBroadcastRepository
import com.zc.bakamitai.feature.home.domain.repository.FakeBroadcastRepository.Call
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchShowReleasesUseCaseTest {

    private val repository = FakeBroadcastRepository()
    private val useCase = SearchShowReleasesUseCase(repository)
    private val utc = TimeZone.UTC

    @Test
    fun `keeps the show's own slug, newest first`() = runTest {
        repository.searches["Kyouran Reijou"] = Result.success(
            listOf(
                release("kyouran-reijou", "04", "2026-10-27T12:58:00Z"),
                release("kyouran-reijou-s2", "01", "2026-11-17T12:00:00Z"),
                release("kyouran-reijou", "05", "2026-11-03T11:59:00Z"),
            ),
        )

        val releases = useCase(ShowSlug("kyouran-reijou"), "Kyouran Reijou", utc).getOrThrow()

        assertEquals(listOf("05", "04"), releases.map { it.episode })
        assertEquals(listOf<Call>(Call.Search("Kyouran Reijou", utc)), repository.calls)
    }

    @Test
    fun `no results is an empty list`() = runTest {
        assertEquals(Result.success(emptyList()), useCase(ShowSlug("x"), "x", utc))
    }

    @Test
    fun `a failure is propagated`() = runTest {
        repository.searches["x"] = Result.failure(DomainError.Timeout)

        assertEquals(DomainError.Timeout, useCase(ShowSlug("x"), "x", utc).exceptionOrNull())
    }
}
