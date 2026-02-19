package com.zc.bakamitai.compose.features.details.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EpisodeDTO(

    @SerialName("release_date")
    val releaseDate: String? = null,

    @SerialName("downloads")
    val downloads: List<DownloadDTO> = emptyList(),

    @SerialName("show")
    val show: String? = null,

    @SerialName("episode")
    val episode: String? = null,

    @SerialName("time")
    val time: String? = null
)
