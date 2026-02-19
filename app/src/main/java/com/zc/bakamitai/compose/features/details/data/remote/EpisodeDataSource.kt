package com.zc.bakamitai.compose.features.details.data.remote

import com.zc.bakamitai.compose.features.details.data.model.EpisodeContentDTO

interface EpisodeDataSource {
    suspend fun getEpisodes(sid: String): EpisodeContentDTO
}
