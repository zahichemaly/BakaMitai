package com.zc.bakamitai.feature.details.domain.repository

import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.details.domain.model.ShowEpisodes
import com.zc.bakamitai.feature.details.domain.model.ShowOverview
import kotlinx.coroutines.CompletableDeferred
import kotlinx.datetime.TimeZone

/**
 * Returns [overviewResult] and [episodesResult], and logs every call.
 *
 * @property overviewGate when set, every overview call suspends until it completes.
 * @property episodesGate when set, every episodes call suspends until it completes.
 */
class FakeShowDetailsRepository : ShowDetailsRepository {

    data class OverviewCall(val slug: ShowSlug, val forceRefresh: Boolean)

    data class EpisodesCall(val slug: ShowSlug, val timeZone: TimeZone, val forceRefresh: Boolean)

    var overviewResult: Result<ShowOverview> = Result.failure(IllegalStateException("No overview set"))
    var episodesResult: Result<ShowEpisodes> = Result.failure(IllegalStateException("No episodes set"))
    var overviewGate: CompletableDeferred<Unit>? = null
    var episodesGate: CompletableDeferred<Unit>? = null

    val overviewCalls = mutableListOf<OverviewCall>()
    val episodesCalls = mutableListOf<EpisodesCall>()

    override suspend fun getOverview(slug: ShowSlug, forceRefresh: Boolean): Result<ShowOverview> {
        overviewCalls += OverviewCall(slug, forceRefresh)
        overviewGate?.await()
        return overviewResult
    }

    override suspend fun getEpisodes(slug: ShowSlug, timeZone: TimeZone, forceRefresh: Boolean): Result<ShowEpisodes> {
        episodesCalls += EpisodesCall(slug, timeZone, forceRefresh)
        episodesGate?.await()
        return episodesResult
    }
}
