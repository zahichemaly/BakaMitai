package com.zc.bakamitai.feature.schedule.domain.model

import com.zc.bakamitai.common.domain.model.ScheduledShow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone

/**
 * The weekly schedule as SubsPlease returns it: every airing show's weekday and time in [timeZone]. A day without
 * shows may be absent.
 */
data class WeeklyTemplate(val timeZone: TimeZone, val showsByDay: Map<DayOfWeek, List<ScheduledShow>>)
