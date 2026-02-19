package com.zc.bakamitai.compose.features.details.data.repository

import com.zc.bakamitai.compose.core.domain.Resource
import com.zc.bakamitai.compose.core.network.NetworkResponse
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
    override suspend fun getShowDetails(page: String): Resource<ShowDetails> {

        return when (val detailsResponse = detailsDataSource.getDetails(page)) {
            is NetworkResponse.Success -> {
                val sid = detailsResponse.data.toShowSID()
                if (sid != null) {
                    // Fetch episodes
                    when (val episodesResponse = episodeDataSource.getEpisodes(sid)) {
                        is NetworkResponse.Success -> {
                            val episodes = episodesResponse.data
                            val showDetails = detailsResponse.data.toShowDetails(page, episodes)
                            Resource.Success(showDetails)
                        }

                        is NetworkResponse.Failure -> {
                            Resource.Failure(
                                message = episodesResponse.error?.message ?: "",
                                code = episodesResponse.error?.code ?: 500
                            )
                        }
                    }
                } else {
                    Resource.Failure(
                        message = "Could not find show ID",
                    )
                }
            }

            is NetworkResponse.Failure -> {
                Resource.Failure(
                    message = detailsResponse.error?.message ?: "",
                    code = detailsResponse.error?.code ?: 500
                )
            }
        }
    }
}
