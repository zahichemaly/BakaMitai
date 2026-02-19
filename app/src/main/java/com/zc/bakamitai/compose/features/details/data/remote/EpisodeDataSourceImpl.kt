package com.zc.bakamitai.compose.features.details.data.remote

import com.zc.bakamitai.compose.core.network.AppClient
import com.zc.bakamitai.compose.features.details.data.model.EpisodeContentDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EpisodeDataSourceImpl(private val appClient: AppClient) : EpisodeDataSource {

    override suspend fun getEpisodes(sid: String): EpisodeContentDTO {
        return withContext(Dispatchers.IO) {
            appClient.get<EpisodeContentDTO>("/api/?f=show") {
                url {
                    parameters.append("sid", sid)
                }
            }
        }
    }
}
