package com.zc.bakamitai.feature.details.domain.usecase

import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.common.domain.util.TimeZoneProvider
import com.zc.bakamitai.feature.details.domain.model.ShowEpisodes
import com.zc.bakamitai.feature.details.domain.repository.ShowDetailsRepository
import org.koin.core.annotation.Factory

/**
 * A show's episodes and batches, each newest first (ties keep the response order), with release times in the
 * device's current zone (context.md §4.4).
 */
@Factory
class GetShowEpisodesUseCase(
    private val repository: ShowDetailsRepository,
    private val timeZoneProvider: TimeZoneProvider,
) {

    /** @param forceRefresh bypass the cache (pull-to-refresh). */
    suspend operator fun invoke(slug: ShowSlug, forceRefresh: Boolean = false): Result<ShowEpisodes> {
        val timeZone = timeZoneProvider.current()
        return repository.getEpisodes(slug, timeZone, forceRefresh).map { loaded ->
            loaded.copy(
                episodes = loaded.episodes.sortedByDescending { it.releasedAt },
                batches = loaded.batches.sortedByDescending { it.releasedAt },
                timeZone = timeZone,
            )
        }
    }
}
