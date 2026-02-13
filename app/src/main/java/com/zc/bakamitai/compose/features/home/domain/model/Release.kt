package com.zc.bakamitai.compose.features.home.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


typealias LatestRelease = Map<String, Release>

@Serializable
data class Release(

    @SerialName("xdcc")
    val xdcc: String,

    @SerialName("release_date")
    val releaseDate: String,

    @SerialName("downloads")
    val downloads: List<DownloadItem>,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("show")
    val show: String,

    @SerialName("episode")
    val episode: String,

    @SerialName("time")
    val time: String,

    @SerialName("page")
    val page: String
)

@Serializable
data class DownloadItem(

    @SerialName("res")
    val res: Int,

    @SerialName("magnet")
    val magnet: String,

    @SerialName("torrent")
    val torrent: String,

    @SerialName("xdcc")
    val xdcc: String
)
