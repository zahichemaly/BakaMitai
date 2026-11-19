package com.zc.bakamitai.feature.details.domain.repository

import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.details.domain.model.ShowEpisodes
import com.zc.bakamitai.feature.details.domain.model.ShowOverview
import kotlinx.datetime.TimeZone

/** A show's page and downloads, cached for a few minutes. Failures are `DomainError`s. */
interface ShowDetailsRepository {

    /** @param forceRefresh bypass the cache (pull-to-refresh). */
    suspend fun getOverview(slug: ShowSlug, forceRefresh: Boolean): Result<ShowOverview>

    /**
     * The show's episodes and batches in response order, with [timeZone] attached.
     *
     * @param forceRefresh bypass the cache (pull-to-refresh).
     */
    suspend fun getEpisodes(slug: ShowSlug, timeZone: TimeZone, forceRefresh: Boolean): Result<ShowEpisodes>
}
