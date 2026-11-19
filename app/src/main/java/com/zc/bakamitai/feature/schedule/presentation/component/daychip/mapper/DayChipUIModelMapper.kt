package com.zc.bakamitai.feature.schedule.presentation.component.daychip.mapper

import com.zc.bakamitai.common.ui.mapper.toDayName
import com.zc.bakamitai.common.ui.mapper.toShortDayName
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.schedule.domain.model.ScheduleDay
import com.zc.bakamitai.feature.schedule.domain.model.WeeklySchedule
import com.zc.bakamitai.feature.schedule.presentation.component.daychip.model.DayChipUIModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate

fun ScheduleDay.toDayChipUIModel(today: LocalDate): DayChipUIModel {
    val isToday = date == today
    val dateLabel = UiText.StringValue(date.day.toString())
    return DayChipUIModel(
        dayOfWeek = dayOfWeek,
        weekdayLabel = dayOfWeek.toShortDayName(),
        dateLabel = dateLabel,
        contentDescription = UiText.StringResource(
            if (isToday) StringResource.cd_schedule_day_today else StringResource.cd_schedule_day,
            listOf(dayOfWeek.toDayName(), dateLabel),
        ),
        isToday = isToday,
    )
}

/** The day tabs, in the week's order. */
fun WeeklySchedule.toDayChipUIModels(): ImmutableList<DayChipUIModel> =
    days.map { it.toDayChipUIModel(today) }.toImmutableList()
