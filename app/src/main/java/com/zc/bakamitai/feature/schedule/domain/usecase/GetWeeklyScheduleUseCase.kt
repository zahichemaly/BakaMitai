package com.zc.bakamitai.feature.schedule.domain.usecase

import com.zc.bakamitai.common.domain.model.ScheduledShow
import com.zc.bakamitai.common.domain.util.TimeZoneProvider
import com.zc.bakamitai.feature.schedule.domain.model.ScheduleDay
import com.zc.bakamitai.feature.schedule.domain.model.WeeklySchedule
import com.zc.bakamitai.feature.schedule.domain.repository.ScheduleRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.offsetAt
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory
import kotlin.time.Clock

/**
 * The current week's schedule in the device's time zone (docs/2-schedule.md §4.4): the 7 days of the week that
 * contains today, from [DEFAULT_WEEK_START] (or the given week start), each with its shows by time then title.
 *
 * The weekly schedule is required. Today's schedule is best effort: when it loads, today's shows get its `aired`
 * flag by slug; other days never have a status, because the weekly schedule has none.
 */
@Factory
class GetWeeklyScheduleUseCase(
    private val repository: ScheduleRepository,
    private val clock: Clock,
    private val timeZoneProvider: TimeZoneProvider,
) {

    /**
     * @param weekStart the first day of the week, the first tab. Monday until the Settings feature passes the
     * user's choice (context.md §3.6).
     * @param forceRefresh bypass the weekly cache (pull-to-refresh).
     */
    suspend operator fun invoke(
        weekStart: DayOfWeek = DEFAULT_WEEK_START,
        forceRefresh: Boolean = false,
    ): Result<WeeklySchedule> = coroutineScope {
        val timeZone = timeZoneProvider.current()
        val now = clock.now()
        val today = now.toLocalDateTime(timeZone).date

        val weeklyRequest = async { repository.getWeeklySchedule(timeZone, forceRefresh) }
        val todayRequest = async { repository.getTodaySchedule(timeZone) }

        val weekly = weeklyRequest.await()
        val todaySchedule = todayRequest.await().getOrDefault(emptyList())

        weekly.map { showsByDay ->
            val airedBySlug = todaySchedule.associate { it.show.slug to it.aired }
            val days = weekDates(today, weekStart).map { date ->
                val shows = showsByDay[date.dayOfWeek].orEmpty()
                    .sortedWith(compareBy<ScheduledShow>({ it.time }, { it.show.title }))
                    .map { show -> show.copy(aired = if (date == today) airedBySlug[show.show.slug] else null) }
                ScheduleDay(date = date, shows = shows)
            }
            WeeklySchedule(today = today, timeZone = timeZone, utcOffset = timeZone.offsetAt(now), days = days)
        }
    }

    /** The dates of the week that contains [today], starting on [weekStart]. */
    private fun weekDates(today: LocalDate, weekStart: DayOfWeek): List<LocalDate> {
        val daysSinceStart = (today.dayOfWeek.isoDayNumber - weekStart.isoDayNumber).mod(DAYS_PER_WEEK)
        val firstDate = today.minus(daysSinceStart, DateTimeUnit.DAY)
        return (0 until DAYS_PER_WEEK).map { firstDate.plus(it, DateTimeUnit.DAY) }
    }

    companion object {
        /** context.md §3.6's default *First day of week*. */
        val DEFAULT_WEEK_START = DayOfWeek.MONDAY

        private const val DAYS_PER_WEEK = 7
    }
}
