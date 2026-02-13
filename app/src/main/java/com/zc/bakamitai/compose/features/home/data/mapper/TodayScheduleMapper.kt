package com.zc.bakamitai.compose.features.home.data.mapper

import com.zc.bakamitai.compose.features.home.data.model.ScheduleDTO
import com.zc.bakamitai.compose.features.home.data.model.TodayScheduleDTO
import com.zc.bakamitai.compose.features.home.domain.model.Schedule
import com.zc.bakamitai.compose.features.home.domain.model.TodaySchedule
import com.zc.bakamitai.extensions.toImageUrl
import timber.log.Timber

fun TodayScheduleDTO.toDomain(): TodaySchedule {
    return TodaySchedule(
        tz = tz,
        schedule = schedule.map { it.toDomain() }
    )
}

private fun ScheduleDTO.toDomain(): Schedule {
    Timber.d("IMAGE URL: ${imageUrl.toImageUrl()}")
    return Schedule(
        aired = aired,
        imageUrl = imageUrl.toImageUrl(),
        page = page,
        time = time,
        title = title
    )
}
