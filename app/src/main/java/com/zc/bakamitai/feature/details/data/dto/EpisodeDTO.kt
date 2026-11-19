package com.zc.bakamitai.feature.details.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One episode or batch of `f=show` (docs/api.md §3.5). `time` is a display label and isn't declared.
 *
 * @property releaseDate RFC 1123, with a per-show offset.
 * @property episode the label as SubsPlease writes it: `"07"`, `"1216v2"`, `"Movie"`, `"01-28"`.
 */
@Serializable
data class EpisodeDTO(
    @SerialName("release_date") val releaseDate: String?,
    @SerialName("show") val show: String?,
    @SerialName("episode") val episode: String?,
    @SerialName("downloads") val downloads: List<DownloadDTO>?,
)
