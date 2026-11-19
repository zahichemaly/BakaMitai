package com.zc.bakamitai.feature.schedule.presentation.component.dayheader.mapper

import com.zc.bakamitai.common.ui.mapper.toDayName
import com.zc.bakamitai.common.ui.mapper.toUtcOffsetLabel
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.schedule.domain.model.ScheduleDay
import com.zc.bakamitai.feature.schedule.presentation.component.dayheader.model.DayHeaderUIModel
import com.zc.bakamitai.feature.schedule.presentation.component.dayheader.model.StatusFilterChipUIModel
import com.zc.bakamitai.feature.schedule.presentation.model.StatusFilter
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.UtcOffset

/** Counts are the whole day's, before filtering. The filter chips exist only when the day has statuses. */
fun ScheduleDay.toDayHeaderUIModel(utcOffset: UtcOffset): DayHeaderUIModel = DayHeaderUIModel(
    title = dayOfWeek.toDayName(),
    subtitle = UiText.StringResource(StringResource.schedule_time_zone, listOf(utcOffset.toUtcOffsetLabel())),
    countLabel = UiText.StringPlural(PluralResource.schedule_show_count, shows.size, listOf(shows.size)),
    filters = if (hasStatuses) {
        persistentListOf(
            StatusFilter.ALL.toChip(count = shows.size),
            StatusFilter.AIRED.toChip(count = shows.count { it.aired == true }),
            StatusFilter.UPCOMING.toChip(count = shows.count { it.aired == false }),
        )
    } else {
        persistentListOf()
    },
)

/** "All (6)", "Aired (5)", "Upcoming (1)". */
private fun StatusFilter.toChip(count: Int) = StatusFilterChipUIModel(
    filter = this,
    label = UiText.StringResource(
        when (this) {
            StatusFilter.ALL -> StringResource.schedule_filter_all
            StatusFilter.AIRED -> StringResource.schedule_filter_aired
            StatusFilter.UPCOMING -> StringResource.schedule_filter_upcoming
        },
        listOf(count),
    ),
)
