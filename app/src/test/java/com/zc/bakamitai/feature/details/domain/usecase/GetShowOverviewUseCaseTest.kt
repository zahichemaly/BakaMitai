package com.zc.bakamitai.feature.details.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.details.domain.DetailsTestData.overview
import com.zc.bakamitai.feature.details.domain.repository.FakeShowDetailsRepository
import com.zc.bakamitai.feature.details.domain.repository.FakeShowDetailsRepository.OverviewCall
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetShowOverviewUseCaseTest {

    private val repository = FakeShowDetailsRepository()
    private val useCase = GetShowOverviewUseCase(repository)
    private val slug = ShowSlug("sora-wa-akai-kawa-no-hotori")

    @Test
    fun `returns the repository's overview, from the cache by default`() = runTest {
        repository.overviewResult = Result.success(overview())

        assertEquals(Result.success(overview()), useCase(slug))
        assertEquals(listOf(OverviewCall(slug, forceRefresh = false)), repository.overviewCalls)
    }

    @Test
    fun `passes forceRefresh on`() = runTest {
        repository.overviewResult = Result.success(overview())

        useCase(slug, forceRefresh = true)

        assertEquals(listOf(OverviewCall(slug, forceRefresh = true)), repository.overviewCalls)
    }

    @Test
    fun `propagates a failure`() = runTest {
        repository.overviewResult = Result.failure(DomainError.NotFound)

        assertEquals(DomainError.NotFound, useCase(slug).exceptionOrNull())
    }
}
