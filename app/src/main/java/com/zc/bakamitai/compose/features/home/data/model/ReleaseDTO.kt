package com.zc.bakamitai.compose.features.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


typealias LatestReleaseDTO = Map<String, ReleaseDTO>

@Serializable
data class ReleaseDTO(

    @SerialName("xdcc")
    val xdcc: String = "",

    @SerialName("release_date")
    val releaseDate: String = "",

    @SerialName("downloads")
    val downloads: List<DownloadItemDTO> = emptyList(),

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("show")
    val show: String = "",

    @SerialName("episode")
    val episode: String = "",

    @SerialName("time")
    val time: String = "",

    @SerialName("page")
    val page: String = ""
)

@Serializable
data class DownloadItemDTO(

    @SerialName("res")
    val res: String = "",

    @SerialName("magnet")
    val magnet: String = "",

    @SerialName("torrent")
    val torrent: String = "",

    @SerialName("xdcc")
    val xdcc: String = ""
)
