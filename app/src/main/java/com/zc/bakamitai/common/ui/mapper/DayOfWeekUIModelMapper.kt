package com.zc.bakamitai.common.ui.mapper

import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import kotlinx.datetime.DayOfWeek

/** The full day name, "Thursday". */
fun DayOfWeek.toDayName(): UiText = UiText.StringResource(
    when (this) {
        DayOfWeek.MONDAY -> StringResource.day_monday
        DayOfWeek.TUESDAY -> StringResource.day_tuesday
        DayOfWeek.WEDNESDAY -> StringResource.day_wednesday
        DayOfWeek.THURSDAY -> StringResource.day_thursday
        DayOfWeek.FRIDAY -> StringResource.day_friday
        DayOfWeek.SATURDAY -> StringResource.day_saturday
        DayOfWeek.SUNDAY -> StringResource.day_sunday
    },
)

/** The short day name, uppercase, "THU". */
fun DayOfWeek.toShortDayName(): UiText = UiText.StringResource(
    when (this) {
        DayOfWeek.MONDAY -> StringResource.day_short_monday
        DayOfWeek.TUESDAY -> StringResource.day_short_tuesday
        DayOfWeek.WEDNESDAY -> StringResource.day_short_wednesday
        DayOfWeek.THURSDAY -> StringResource.day_short_thursday
        DayOfWeek.FRIDAY -> StringResource.day_short_friday
        DayOfWeek.SATURDAY -> StringResource.day_short_saturday
        DayOfWeek.SUNDAY -> StringResource.day_short_sunday
    },
)
