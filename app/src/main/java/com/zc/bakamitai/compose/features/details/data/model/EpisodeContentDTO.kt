package com.zc.bakamitai.compose.features.details.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EpisodeContentDTO(
    @SerialName("episode")
    val episode: Map<String, EpisodeDTO> = emptyMap()
)
