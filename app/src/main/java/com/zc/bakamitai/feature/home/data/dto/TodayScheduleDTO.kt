package com.zc.bakamitai.feature.home.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `f=schedule&h=true` (docs/api.md §3.1). */
@Serializable
data class TodayScheduleDTO(@SerialName("schedule") val schedule: List<ScheduleItemDTO>?)
