package com.zc.bakamitai.data.models

import com.google.gson.annotations.SerializedName
import com.zc.bakamitai.data.models.dtos.EntryDto
import com.zc.bakamitai.data.models.dtos.ScheduleDto
import com.zc.bakamitai.data.room.entities.Schedule
import com.zc.bakamitai.extensions.toDayOfWeekNumber
import com.zc.bakamitai.extensions.toImageUrl
import java.util.*

data class ScheduleResponse(

    @field:SerializedName("schedule")
    val schedule: HashMap<String, List<ScheduleItem>>,

    @field:SerializedName("tz")
    val tz: String
) {

    fun toScheduleDtoList(): List<ScheduleDto> {
        return schedule.map {
            ScheduleDto(
                day = it.key,
                entries = it.value
                    .sortedBy { item -> item.time }
                    .map { item -> item.toEntryDto() }
            )
        }
    }

    fun toScheduleEntities(): List<Schedule> {
        val entities = mutableListOf<Schedule>()
        val timeZone = TimeZone.getTimeZone(tz)
        val calendar = Calendar.getInstance(timeZone)
        val now = Date()
        calendar.time = now
        schedule.forEach {
            val day = it.key.toDayOfWeekNumber(false)
            calendar.set(Calendar.DAY_OF_WEEK, day)
            val scheduleItems = it.value
            scheduleItems.forEach { item ->
                val times = item.time.split(":")
                if (times.size == 2) {
                    calendar.set(Calendar.HOUR_OF_DAY, times[0].toInt())
                    calendar.set(Calendar.MINUTE, times[1].toInt())
                    val date = calendar.time
                    entities.add(
                        Schedule(
                            id = item.page,
                            name = item.title,
                            date = date.time
                        )
                    )
                }
            }
        }
        return entities
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

    fun toEntryDto(): EntryDto {
        return EntryDto(
            time = time,
            name = title,
            imageUrl = imageUrl.toImageUrl(),
            aired = aired,
            page = page
        )
    }
}
