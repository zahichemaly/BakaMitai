package com.zc.bakamitai.feature.shows.domain.repository

import com.zc.bakamitai.common.domain.model.Show
import kotlinx.coroutines.CompletableDeferred

/**
 * Returns [result] (an empty success by default) and logs every call's `forceRefresh`.
 *
 * @property gate when set, every call suspends until it completes.
 */
class FakeShowCatalogRepository : ShowCatalogRepository {

    var result: Result<List<Show>> = Result.success(emptyList())
    var gate: CompletableDeferred<Unit>? = null

    /** The `forceRefresh` of every call, in call order. */
    val forceRefreshCalls = mutableListOf<Boolean>()

    override suspend fun getAllShows(forceRefresh: Boolean): Result<List<Show>> {
        forceRefreshCalls += forceRefresh
        gate?.await()
        return result
    }
}
