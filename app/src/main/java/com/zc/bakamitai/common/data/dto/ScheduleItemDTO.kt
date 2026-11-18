package com.zc.bakamitai.common.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One show on the schedule. `time` is `"HH:mm"` in the requested zone; `aired` is only on today's schedule. */
@Serializable
data class ScheduleItemDTO(
    @SerialName("title") val title: String?,
    @SerialName("page") val page: String?,
    @SerialName("image_url") val imageUrl: String?,
    @SerialName("time") val time: String?,
    @SerialName("aired") val aired: Boolean?,
)
