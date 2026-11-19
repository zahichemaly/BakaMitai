package com.zc.bakamitai.feature.details.domain.usecase

import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.details.domain.model.ShowOverview
import com.zc.bakamitai.feature.details.domain.repository.ShowDetailsRepository
import org.koin.core.annotation.Factory

/** A show's title, cover and synopsis, from its page. */
@Factory
class GetShowOverviewUseCase(private val repository: ShowDetailsRepository) {

    /** @param forceRefresh bypass the cache (pull-to-refresh). */
    suspend operator fun invoke(slug: ShowSlug, forceRefresh: Boolean = false): Result<ShowOverview> =
        repository.getOverview(slug, forceRefresh)
}
