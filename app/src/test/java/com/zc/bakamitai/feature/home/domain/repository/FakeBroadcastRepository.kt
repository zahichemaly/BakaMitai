package com.zc.bakamitai.feature.home.domain.repository

import com.zc.bakamitai.feature.home.domain.model.Release
import com.zc.bakamitai.feature.home.domain.model.ScheduledShow
import kotlinx.coroutines.CompletableDeferred
import kotlinx.datetime.TimeZone

/**
 * Returns the configured result per call (an empty success by default) and logs every call.
 *
 * @property gate when set, every call suspends until it completes.
 * @property showSearchGate when set, searches with a non-empty query (episode lookups) suspend until it completes.
 */
class FakeBroadcastRepository : BroadcastRepository {

    sealed interface Call {
        val timeZone: TimeZone

        data class Schedule(override val timeZone: TimeZone) : Call

        data class Latest(val page: Int, override val timeZone: TimeZone) : Call

        data class Search(val query: String, override val timeZone: TimeZone) : Call
    }

    var schedule: Result<List<ScheduledShow>> = Result.success(emptyList())
    val latestPages = mutableMapOf<Int, Result<List<Release>>>()
    val searches = mutableMapOf<String, Result<List<Release>>>()

    var gate: CompletableDeferred<Unit>? = null
    var showSearchGate: CompletableDeferred<Unit>? = null

    val calls = mutableListOf<Call>()

    /** Searches whose call has returned, in completion order. */
    val completedSearches = mutableListOf<String>()

    override suspend fun getTodaySchedule(timeZone: TimeZone): Result<List<ScheduledShow>> {
        calls += Call.Schedule(timeZone)
        gate?.await()
        return schedule
    }

    override suspend fun getLatestReleases(page: Int, timeZone: TimeZone): Result<List<Release>> {
        calls += Call.Latest(page, timeZone)
        gate?.await()
        return latestPages[page] ?: Result.success(emptyList())
    }

    override suspend fun searchReleases(query: String, timeZone: TimeZone): Result<List<Release>> {
        calls += Call.Search(query, timeZone)
        gate?.await()
        if (query.isNotEmpty()) showSearchGate?.await()
        completedSearches += query
        return searches[query] ?: Result.success(emptyList())
    }
}
