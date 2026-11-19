package com.zc.bakamitai.common.domain.model

import kotlinx.datetime.DayOfWeek

/** The first day of the week: Schedule's first tab. Stored by name: never rename a constant. */
enum class WeekStart(val dayOfWeek: DayOfWeek) {
    MONDAY(DayOfWeek.MONDAY),
    SUNDAY(DayOfWeek.SUNDAY),
}
