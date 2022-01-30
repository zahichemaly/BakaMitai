package com.zc.bakamitai.data.models

import com.google.gson.annotations.SerializedName
import com.zc.bakamitai.data.Constants
import com.zc.bakamitai.data.models.dtos.EntryDto
import com.zc.bakamitai.data.models.dtos.ScheduleDto
import com.zc.bakamitai.data.room.entities.Schedule
import com.zc.bakamitai.extensions.isDayOfWeek
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
            val day = it.key
            val isDayOfWeek = day.isDayOfWeek()
            ScheduleDto(
                day = day,
                entries = it.value
                    .sortedBy { item -> item.time }
                    .map { item -> item.toEntryDto(isDayOfWeek) }
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
                val date = if (times.size == 2) {
                    calendar.set(Calendar.HOUR_OF_DAY, times[0].toInt())
                    calendar.set(Calendar.MINUTE, times[1].toInt())
                    calendar.time
                } else {
                    //Unscheduled shows with invalid date
                    Date()
                }
                entities.add(
                    Schedule(
                        page = item.page,
                        name = item.title,
                        date = date.time
                    )
                )
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

    fun toEntryDto(isDayOfWeek: Boolean = true): EntryDto {
        return EntryDto(
            time = if (isDayOfWeek) time else Constants.Common.NOT_AVAILABLE,
            name = title,
            imageUrl = imageUrl.toImageUrl(),
            aired = aired,
            page = page
        )
    }
}
