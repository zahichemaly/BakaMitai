package com.zc.bakamitai.compose.features.details.data.repository

import com.zc.bakamitai.compose.core.domain.Resource
import com.zc.bakamitai.compose.core.network.NetworkResponse
import com.zc.bakamitai.compose.features.details.data.mapper.toShowDetails
import com.zc.bakamitai.compose.features.details.data.remote.DetailsDataSource
import com.zc.bakamitai.compose.features.details.data.remote.EpisodeDataSource
import com.zc.bakamitai.compose.features.details.domain.model.ShowDetails
import com.zc.bakamitai.compose.features.details.domain.repository.ShowDetailsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope

class ShowDetailsRepositoryImpl(
    private val detailsDataSource: DetailsDataSource,
    private val episodeDataSource: EpisodeDataSource,
) : ShowDetailsRepository {
    override suspend fun getShowDetails(page: String): Resource<ShowDetails> {

        return supervisorScope {
            val detailsDef = async { detailsDataSource.getDetails(page) }
            val episodesDef = async { episodeDataSource.getEpisodes(page) }

            val detailsResponse = detailsDef.await()
            val episodesResponse = episodesDef.await()

            if (detailsResponse is NetworkResponse.Success && episodesResponse is NetworkResponse.Success) {
                val episodes = episodesResponse.data
                val showDetails = detailsResponse.data.toShowDetails(page, episodes)
                Resource.Success(data = showDetails)
            } else {
                Resource.Failure(
                    message = "Something went wrong",
                    code = 500
                )
            }
        }
    }
}
