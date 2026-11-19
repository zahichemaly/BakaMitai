package com.zc.bakamitai.feature.shows.data.repository

import com.zc.bakamitai.common.data.util.safeApiCall
import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.common.domain.util.DispatcherProvider
import com.zc.bakamitai.feature.shows.data.mapper.toDomain
import com.zc.bakamitai.feature.shows.data.remote.AllShowsHtmlParser
import com.zc.bakamitai.feature.shows.data.remote.ShowsService
import com.zc.bakamitai.feature.shows.domain.repository.ShowCatalogRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

/**
 * Keeps the last successful catalogue in memory for the process (docs/api.md §7); pull-to-refresh replaces it.
 * Failures aren't cached and keep the previous catalogue. Concurrent callers wait for the lock, so they share one
 * request. The ~200 KiB page is parsed on the default dispatcher.
 */
@Single
class ShowCatalogRepositoryImpl(
    private val service: ShowsService,
    private val parser: AllShowsHtmlParser,
    private val dispatchers: DispatcherProvider,
) : ShowCatalogRepository {

    private val mutex = Mutex()
    private var cache: List<Show>? = null

    override suspend fun getAllShows(forceRefresh: Boolean): Result<List<Show>> = mutex.withLock {
        val cached = cache?.takeUnless { forceRefresh }
        if (cached != null) {
            Result.success(cached)
        } else {
            safeApiCall {
                val html = service.getAllShowsPage()
                withContext(dispatchers.default) { parser.parse(html).toDomain() }
                    // SubsPlease always lists shows: a page without any means its markup changed.
                    .ifEmpty { throw DomainError.InvalidData }
            }.onSuccess { shows -> cache = shows }
        }
    }
}
