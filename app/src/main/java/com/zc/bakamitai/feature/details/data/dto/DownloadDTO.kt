package com.zc.bakamitai.feature.details.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One resolution of an `f=show` entry (docs/api.md §3.5–3.6).
 *
 * @property res the height as a string: `"360"`, `"480"`, `"540"`, `"720"` or `"1080"`.
 * @property torrent the nyaa.si `.torrent` URL.
 * @property xdcc the XDCC search string, already URL-encoded.
 */
@Serializable
data class DownloadDTO(
    @SerialName("res") val res: String?,
    @SerialName("magnet") val magnet: String?,
    @SerialName("torrent") val torrent: String?,
    @SerialName("xdcc") val xdcc: String?,
)
