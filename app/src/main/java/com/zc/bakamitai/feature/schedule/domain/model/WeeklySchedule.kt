package com.zc.bakamitai.feature.schedule.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset

/**
 * The current week's schedule in [timeZone] (docs/2-schedule.md §4.4).
 *
 * @property today today's date in [timeZone] when the data was loaded.
 * @property utcOffset the offset of [timeZone] when the data was loaded.
 * @property days the 7 days of the week containing [today], in tab order (from the week start).
 */
data class WeeklySchedule(
    val today: LocalDate,
    val timeZone: TimeZone,
    val utcOffset: UtcOffset,
    val days: List<ScheduleDay>,
)
