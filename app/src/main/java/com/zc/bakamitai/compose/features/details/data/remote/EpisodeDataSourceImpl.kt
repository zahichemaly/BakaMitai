package com.zc.bakamitai.compose.features.details.data.remote

import com.zc.bakamitai.compose.core.network.GenericResponse
import com.zc.bakamitai.compose.core.network.HttpClientWrapper
import com.zc.bakamitai.compose.features.details.data.model.EpisodeContentDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EpisodeDataSourceImpl(private val httpClientWrapper: HttpClientWrapper) : EpisodeDataSource {

    override suspend fun getEpisodes(sid: String): GenericResponse<EpisodeContentDTO> {
        return withContext(Dispatchers.IO) {
            httpClientWrapper.get<EpisodeContentDTO>("/api/?f=show") {
                url {
                    parameters.append("sid", sid)
                }
            }
        }
    }
}
