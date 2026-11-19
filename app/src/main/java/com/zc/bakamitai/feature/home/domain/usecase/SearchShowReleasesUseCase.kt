package com.zc.bakamitai.feature.home.domain.usecase

import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.home.domain.model.Release
import com.zc.bakamitai.feature.home.domain.repository.BroadcastRepository
import kotlinx.datetime.TimeZone
import org.koin.core.annotation.Factory

/**
 * One show's releases, newest first: a title search for [query] (the feeds' title, which can differ from the show
 * page's), keeping only [slug] because a search also returns other seasons.
 */
@Factory
class SearchShowReleasesUseCase(private val repository: BroadcastRepository) {

    suspend operator fun invoke(slug: ShowSlug, query: String, timeZone: TimeZone): Result<List<Release>> =
        repository.searchReleases(query = query, timeZone = timeZone).map { releases ->
            releases.filter { it.show.slug == slug }.sortedByDescending { it.releasedAt }
        }
}
