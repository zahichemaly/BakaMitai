package com.zc.bakamitai.feature.schedule.domain.repository

import com.zc.bakamitai.common.domain.model.ScheduledShow
import kotlinx.coroutines.CompletableDeferred
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone

/**
 * Returns the configured results (empty successes by default) and logs every call.
 *
 * @property gate when set, every call suspends until it completes.
 */
class FakeScheduleRepository : ScheduleRepository {

    sealed interface Call {
        val timeZone: TimeZone

        data class Weekly(override val timeZone: TimeZone, val forceRefresh: Boolean) : Call

        data class Today(override val timeZone: TimeZone) : Call
    }

    var weekly: Result<Map<DayOfWeek, List<ScheduledShow>>> = Result.success(emptyMap())
    var today: Result<List<ScheduledShow>> = Result.success(emptyList())
    var gate: CompletableDeferred<Unit>? = null

    val calls = mutableListOf<Call>()

    /** Calls whose result has been returned, in completion order. */
    val completed = mutableListOf<Call>()

    override suspend fun getWeeklySchedule(
        timeZone: TimeZone,
        forceRefresh: Boolean,
    ): Result<Map<DayOfWeek, List<ScheduledShow>>> {
        val call = Call.Weekly(timeZone, forceRefresh)
        calls += call
        gate?.await()
        completed += call
        return weekly
    }

    override suspend fun getTodaySchedule(timeZone: TimeZone): Result<List<ScheduledShow>> {
        val call = Call.Today(timeZone)
        calls += call
        gate?.await()
        completed += call
        return today
    }
}
