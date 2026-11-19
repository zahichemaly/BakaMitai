package com.zc.bakamitai.feature.schedule.presentation.mapper

import com.zc.bakamitai.common.domain.model.ScheduledShow
import com.zc.bakamitai.common.ui.mapper.toDayName
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.schedule.domain.model.ScheduleDay
import com.zc.bakamitai.feature.schedule.presentation.component.dayheader.mapper.toDayHeaderUIModel
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.mapper.toScheduleCardUIModel
import com.zc.bakamitai.feature.schedule.presentation.model.ScheduleDayUIModel
import com.zc.bakamitai.feature.schedule.presentation.model.ScheduleEmptyUIModel
import com.zc.bakamitai.feature.schedule.presentation.model.StatusFilter
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.UtcOffset

/**
 * The day as shown: its header, the cards that pass [filter], and the empty message when none do. The filter
 * applies only to a day with statuses (today); any other day shows every show.
 */
fun ScheduleDay.toUIModel(utcOffset: UtcOffset, filter: StatusFilter): ScheduleDayUIModel {
    val visible = if (hasStatuses) shows.filter { it.matches(filter) } else shows
    val empty = when {
        shows.isEmpty() -> ScheduleEmptyUIModel(
            title = UiText.StringResource(StringResource.schedule_day_empty_title),
            message = UiText.StringResource(StringResource.schedule_day_empty_message, listOf(dayOfWeek.toDayName())),
            showResetAction = false,
        )

        visible.isEmpty() -> ScheduleEmptyUIModel(
            title = UiText.StringResource(StringResource.schedule_filter_empty_title),
            message = UiText.StringResource(StringResource.schedule_filter_empty_message),
            showResetAction = true,
        )

        else -> null
    }
    return ScheduleDayUIModel(
        header = toDayHeaderUIModel(utcOffset),
        cards = visible.map { it.toScheduleCardUIModel() }.toImmutableList(),
        empty = empty,
    )
}

private fun ScheduledShow.matches(filter: StatusFilter): Boolean = when (filter) {
    StatusFilter.ALL -> true
    StatusFilter.AIRED -> aired == true
    StatusFilter.UPCOMING -> aired == false
}
