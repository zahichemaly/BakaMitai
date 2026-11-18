package com.zc.bakamitai.feature.home.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One release from `f=latest` or `f=search` (docs/api.md §3.3). `time` is a display label that is never
 * parsed; `downloads` and `xdcc` aren't declared because Home doesn't use them.
 */
@Serializable
data class ReleaseDTO(
    @SerialName("time") val time: String?,
    @SerialName("release_date") val releaseDate: String?,
    @SerialName("show") val show: String?,
    @SerialName("episode") val episode: String?,
    @SerialName("image_url") val imageUrl: String?,
    @SerialName("page") val page: String?,
)
