package com.zc.bakamitai.feature.details.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.common.domain.util.FixedTimeZoneProvider
import com.zc.bakamitai.feature.details.domain.DetailsTestData.episode
import com.zc.bakamitai.feature.details.domain.DetailsTestData.episodes
import com.zc.bakamitai.feature.details.domain.repository.FakeShowDetailsRepository
import com.zc.bakamitai.feature.details.domain.repository.FakeShowDetailsRepository.EpisodesCall
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class GetShowEpisodesUseCaseTest {

    private val tokyo = TimeZone.of("Asia/Tokyo")
    private val repository = FakeShowDetailsRepository()
    private val timeZoneProvider = FixedTimeZoneProvider(tokyo)
    private val useCase = GetShowEpisodesUseCase(repository, timeZoneProvider)
    private val slug = ShowSlug("detective-conan")

    @Test
    fun `sorts episodes and batches newest first`() = runTest {
        repository.episodesResult = Result.success(
            episodes(
                episodes = listOf(
                    episode("1216", releasedAt = "2026-10-10T16:15:03Z"),
                    episode("1220", releasedAt = "2026-11-14T11:34:13Z"),
                    episode("1216v2", releasedAt = "2026-10-17T18:31:00Z"),
                    episode("1217", releasedAt = "2026-10-17T11:30:53Z"),
                ),
                batches = listOf(
                    episode("01-12", releasedAt = "2021-01-05T01:00:27Z"),
                    episode("13-24", releasedAt = "2021-07-05T01:00:27Z"),
                ),
            ),
        )

        val sorted = useCase(slug).getOrThrow()

        assertEquals(listOf("1220", "1216v2", "1217", "1216"), sorted.episodes.map { it.label })
        assertEquals(listOf("13-24", "01-12"), sorted.batches.map { it.label })
    }

    @Test
    fun `ties keep the repository's order`() = runTest {
        repository.episodesResult = Result.success(
            episodes(episodes = listOf(episode("b"), episode("a"), episode("c"))),
        )

        assertEquals(listOf("b", "a", "c"), useCase(slug).getOrThrow().episodes.map { it.label })
    }

    @Test
    fun `asks for and returns the device's current zone`() = runTest {
        repository.episodesResult = Result.success(episodes())

        val loaded = useCase(slug, forceRefresh = true).getOrThrow()

        assertEquals(tokyo, loaded.timeZone)
        assertEquals(listOf(EpisodesCall(slug, tokyo, forceRefresh = true)), repository.episodesCalls)
    }

    @Test
    fun `propagates a failure`() = runTest {
        repository.episodesResult = Result.failure(DomainError.Network)

        assertEquals(DomainError.Network, useCase(slug).exceptionOrNull())
    }
}
