package com.zc.bakamitai.data.models.dtos

data class ScheduleDto(
    val day: String,
    var entries: List<EntryDto>
)
