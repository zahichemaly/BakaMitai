package com.zc.bakamitai.data.models

import com.google.gson.annotations.SerializedName
import com.zc.bakamitai.data.models.dtos.EntryDto
import com.zc.bakamitai.data.models.dtos.ScheduleDto
import com.zc.bakamitai.extensions.to12HourFormat
import com.zc.bakamitai.extensions.toDayOfWeekNumber
import com.zc.bakamitai.extensions.toImageUrl

data class ScheduleResponse(

    @field:SerializedName("schedule")
    val schedule: HashMap<String, List<ScheduleItem>>,

    @field:SerializedName("tz")
    val tz: String
) {

    fun toScheduleDtoList(use12HourFormat: Boolean, startsMonday: Boolean): List<ScheduleDto> {
        return schedule.map {
            ScheduleDto(
                day = it.key,
                entries = it.value
                    .sortedBy { item -> item.time }
                    .map { item -> item.toEntryDto(use12HourFormat) }
            )
        }.sortedBy { it.day.toDayOfWeekNumber(startsMonday) }
    }
}

data class ScheduleItem(

    @field:SerializedName("image_url")
    val imageUrl: String,

    @field:SerializedName("page")
    val page: String,

    @field:SerializedName("time")
    val time: String,

    @field:SerializedName("title")
    val title: String,

    @field:SerializedName("aired")
    val aired: Boolean
) {

    fun toEntryDto(use12HourFormat: Boolean): EntryDto {
        val formattedTime = if (use12HourFormat) time.to12HourFormat() else time
        return EntryDto(
            time = formattedTime,
            name = title,
            imageUrl = imageUrl.toImageUrl(),
            aired = aired,
            page = page
        )
    }
}
