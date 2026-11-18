package com.zc.bakamitai.feature.schedule.domain.repository

import com.zc.bakamitai.common.domain.model.ScheduledShow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone

/** SubsPlease's schedules. Every call is made for a [TimeZone] and fails with a `DomainError`. */
interface ScheduleRepository {

    /**
     * The weekly schedule in [timeZone], by weekday, with times in that zone. A day without shows may be absent.
     * Served from a short-lived cache unless [forceRefresh].
     */
    suspend fun getWeeklySchedule(
        timeZone: TimeZone,
        forceRefresh: Boolean,
    ): Result<Map<DayOfWeek, List<ScheduledShow>>>

    /** Today's schedule in [timeZone], with each show's `aired` flag. Never cached. */
    suspend fun getTodaySchedule(timeZone: TimeZone): Result<List<ScheduledShow>>
}
