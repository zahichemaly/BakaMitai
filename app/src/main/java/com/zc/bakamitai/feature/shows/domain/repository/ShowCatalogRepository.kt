package com.zc.bakamitai.feature.shows.domain.repository

import com.zc.bakamitai.common.domain.model.Show

/** Every show on SubsPlease. Failures are `DomainError`s. */
interface ShowCatalogRepository {

    /**
     * Every listed show, in the source's order, without covers. Several titles may share one slug. Served from a
     * cache kept for the process unless [forceRefresh]. A source without shows fails with `DomainError.InvalidData`.
     */
    suspend fun getAllShows(forceRefresh: Boolean): Result<List<Show>>
}
