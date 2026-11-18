package com.zc.bakamitai.feature.schedule.data.mapper

import com.zc.bakamitai.common.data.mapper.toDomain
import com.zc.bakamitai.common.domain.model.ScheduledShow
import com.zc.bakamitai.feature.schedule.data.dto.WeeklyScheduleDTO
import kotlinx.datetime.DayOfWeek

/**
 * The shows per weekday. Keys are matched to [DayOfWeek] by English name, ignoring case and surrounding spaces;
 * unknown keys are dropped. Items that can't be shown (no slug, title or time) are dropped. A day missing from
 * the response is absent from the map. Not sorted: the order within a day is the use case's job.
 */
fun WeeklyScheduleDTO.toDomain(): Map<DayOfWeek, List<ScheduledShow>> = buildMap {
    days.forEach { (key, items) ->
        val day = key.toDayOfWeek() ?: return@forEach
        put(day, getOrElse(day) { emptyList() } + items.mapNotNull { it.toDomain() })
    }
}

private fun String.toDayOfWeek(): DayOfWeek? {
    val name = trim()
    return DayOfWeek.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
}
