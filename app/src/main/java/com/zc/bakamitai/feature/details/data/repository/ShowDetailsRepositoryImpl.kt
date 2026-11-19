package com.zc.bakamitai.feature.details.data.repository

import com.zc.bakamitai.common.data.util.safeApiCall
import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.common.domain.util.DispatcherProvider
import com.zc.bakamitai.feature.details.data.mapper.toDomain
import com.zc.bakamitai.feature.details.data.mapper.toSid
import com.zc.bakamitai.feature.details.data.remote.ShowDetailsService
import com.zc.bakamitai.feature.details.data.remote.ShowPageHtmlParser
import com.zc.bakamitai.feature.details.domain.model.ShowEpisodes
import com.zc.bakamitai.feature.details.domain.model.ShowOverview
import com.zc.bakamitai.feature.details.domain.repository.ShowDetailsRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import org.koin.core.annotation.Single
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** How long a show's page and episodes are served from memory (docs/api.md §7). */
private val DetailsCacheTtl = 10.minutes

/**
 * Keeps each show's page and episodes in memory for [DetailsCacheTtl] (docs/4-details.md §4.5); pull-to-refresh
 * bypasses it. Failures aren't cached and keep older entries. Expired entries are pruned when their map is
 * written, so the maps stay small. The page is parsed on the default dispatcher.
 */
@Single
class ShowDetailsRepositoryImpl(
    private val service: ShowDetailsService,
    private val parser: ShowPageHtmlParser,
    private val dispatchers: DispatcherProvider,
    private val clock: Clock,
) : ShowDetailsRepository {

    private class CachedPage(val overview: ShowOverview, val sid: String, val fetchedAt: Instant)

    private class CachedEpisodes(val episodes: ShowEpisodes, val fetchedAt: Instant)

    private val mutex = Mutex()
    private val pages = mutableMapOf<ShowSlug, CachedPage>()
    private val episodes = mutableMapOf<ShowSlug, CachedEpisodes>()

    override suspend fun getOverview(slug: ShowSlug, forceRefresh: Boolean): Result<ShowOverview> = mutex.withLock {
        val cached = pages[slug]?.takeIf { !forceRefresh && isFresh(it.fetchedAt) }
        if (cached != null) {
            Result.success(cached.overview)
        } else {
            safeApiCall { fetchPage(slug).overview }
        }
    }

    /**
     * Needs the page's `sid`: the cached page's, even an expired one (a show's `sid` never changes), or else a page
     * fetch, which also fills the page cache. Cached episodes are returned with the caller's [timeZone], since the
     * response doesn't depend on it.
     */
    override suspend fun getEpisodes(slug: ShowSlug, timeZone: TimeZone, forceRefresh: Boolean): Result<ShowEpisodes> =
        mutex.withLock {
            val cached = episodes[slug]?.takeIf { !forceRefresh && isFresh(it.fetchedAt) }
            if (cached != null) {
                Result.success(cached.episodes.copy(timeZone = timeZone))
            } else {
                safeApiCall {
                    val sid = pages[slug]?.sid ?: fetchPage(slug).sid
                    val loaded = service.getShowEpisodes(sid, timeZone.id).toDomain(timeZone)
                    val now = clock.now()
                    episodes.entries.removeAll { !isFresh(it.value.fetchedAt, now) }
                    episodes[slug] = CachedEpisodes(loaded, fetchedAt = now)
                    loaded
                }
            }
        }

    /**
     * Fetches, parses and caches [slug]'s page. A page without a title or a numeric `sid` is
     * [DomainError.InvalidData]. Not locked: the callers hold [mutex], which isn't reentrant.
     */
    private suspend fun fetchPage(slug: ShowSlug): CachedPage {
        val html = service.getShowPage(slug.value)
        val page = withContext(dispatchers.default) { parser.parse(html) }
        val overview = page.toDomain(slug) ?: throw DomainError.InvalidData
        val sid = page.toSid() ?: throw DomainError.InvalidData
        val now = clock.now()
        pages.entries.removeAll { !isFresh(it.value.fetchedAt, now) }
        return CachedPage(overview, sid, fetchedAt = now).also { pages[slug] = it }
    }

    private fun isFresh(fetchedAt: Instant, now: Instant = clock.now()): Boolean = now - fetchedAt < DetailsCacheTtl
}
