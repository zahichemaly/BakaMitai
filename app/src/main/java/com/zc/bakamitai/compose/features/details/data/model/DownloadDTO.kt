package com.zc.bakamitai.compose.features.details.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DownloadDTO(

    @SerialName("res")
    val res: String,

    @SerialName("xdcc")
    val xdcc: String,

    @SerialName("torrent")
    val torrent: String,

    @SerialName("magnet")
    val magnet: String
)

