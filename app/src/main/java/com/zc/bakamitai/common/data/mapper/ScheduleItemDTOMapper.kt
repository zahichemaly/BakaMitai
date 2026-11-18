package com.zc.bakamitai.common.data.mapper

import com.zc.bakamitai.common.data.dto.ScheduleItemDTO
import com.zc.bakamitai.common.data.dto.TodayScheduleDTO
import com.zc.bakamitai.common.domain.model.ScheduledShow
import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.common.domain.model.ShowSlug
import kotlinx.datetime.LocalTime

/** `null` when the item has no slug, no title or no `"HH:mm"` time: it can't be shown or opened. */
fun ScheduleItemDTO.toDomain(): ScheduledShow? {
    val slug = page?.takeIf { it.isNotBlank() }
    val title = title?.takeIf { it.isNotBlank() }
    val time = time?.let { LocalTime.Formats.ISO.parseOrNull(it.trim()) }
    return if (slug != null && title != null && time != null) {
        ScheduledShow(
            show = Show(slug = ShowSlug(slug), title = title, coverUrl = imageUrl.toCoverUrl()),
            time = time,
            aired = aired,
        )
    } else {
        null
    }
}

fun TodayScheduleDTO.toDomain(): List<ScheduledShow> = schedule.orEmpty().mapNotNull { it.toDomain() }
