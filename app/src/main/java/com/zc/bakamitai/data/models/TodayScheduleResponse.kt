package com.zc.bakamitai.data.models

import com.google.gson.annotations.SerializedName

data class TodayScheduleResponse(
    @field:SerializedName("schedule")
    val schedule: List<ScheduleItem> = listOf(),

    @field:SerializedName("tz")
    val tz: String
)
