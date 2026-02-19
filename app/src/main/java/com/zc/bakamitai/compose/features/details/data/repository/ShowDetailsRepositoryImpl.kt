package com.zc.bakamitai.compose.features.details.data.repository

import com.zc.bakamitai.compose.features.details.data.mapper.toShowDetails
import com.zc.bakamitai.compose.features.details.data.mapper.toShowSID
import com.zc.bakamitai.compose.features.details.data.remote.DetailsDataSource
import com.zc.bakamitai.compose.features.details.data.remote.EpisodeDataSource
import com.zc.bakamitai.compose.features.details.domain.model.ShowDetails
import com.zc.bakamitai.compose.features.details.domain.repository.ShowDetailsRepository

class ShowDetailsRepositoryImpl(
    private val detailsDataSource: DetailsDataSource,
    private val episodeDataSource: EpisodeDataSource,
) : ShowDetailsRepository {
    override suspend fun getShowDetails(page: String): ShowDetails {

        val detailsResponse = detailsDataSource.getDetails(page)
        val sid = detailsResponse.toShowSID()
        if (sid.isNullOrBlank()) throw IllegalArgumentException("SID is null or blank")

        val episodeContent = episodeDataSource.getEpisodes(sid)
        return detailsResponse.toShowDetails(
            page = page,
            episodeContent = episodeContent
        )
    }
}
