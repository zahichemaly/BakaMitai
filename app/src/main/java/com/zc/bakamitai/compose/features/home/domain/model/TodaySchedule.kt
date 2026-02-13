package com.zc.bakamitai.compose.features.home.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TodaySchedule(

    @SerialName("schedule")
    val schedule: List<Schedule> = emptyList(),

    @SerialName("tz")
    val tz: String = ""
)

@Serializable
data class Schedule(

    @SerialName("aired")
    val aired: Boolean = false,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("page")
    val page: String = "",

    @SerialName("time")
    val time: String = "",

    @SerialName("title")
    val title: String = ""
)
