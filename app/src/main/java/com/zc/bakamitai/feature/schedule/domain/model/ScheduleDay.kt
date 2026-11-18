package com.zc.bakamitai.feature.schedule.domain.model

import com.zc.bakamitai.common.domain.model.ScheduledShow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * One day of the week being shown.
 *
 * @property date this week's date for the day, in the device's time zone.
 * @property shows the day's shows, by time then title. Only today's carry `aired`.
 */
data class ScheduleDay(val date: LocalDate, val shows: List<ScheduledShow>) {

    val dayOfWeek: DayOfWeek get() = date.dayOfWeek

    /** Whether any show has an Aired / Upcoming status: only today, when today's schedule loaded. */
    val hasStatuses: Boolean get() = shows.any { it.aired != null }
}
