package com.zc.bakamitai.feature.home.domain.usecase

import com.zc.bakamitai.feature.home.domain.model.Release
import com.zc.bakamitai.feature.home.domain.repository.BroadcastRepository
import kotlinx.datetime.TimeZone
import org.koin.core.annotation.Factory

/**
 * The newest releases of every show, in one request: the empty search (about the last two days, never behind),
 * or the first latest page when the search fails (docs/5-bookmarks.md §5.2). `f=latest` can lag by days, so it's
 * only the fallback.
 */
@Factory
class GetNewestReleasesUseCase(private val repository: BroadcastRepository) {

    suspend operator fun invoke(timeZone: TimeZone): Result<List<Release>> {
        val newest = repository.searchReleases(query = NEWEST_QUERY, timeZone = timeZone)
        return if (newest.isSuccess) newest else repository.getLatestReleases(page = FIRST_PAGE, timeZone = timeZone)
    }

    private companion object {
        const val NEWEST_QUERY = ""
        const val FIRST_PAGE = 1
    }
}
