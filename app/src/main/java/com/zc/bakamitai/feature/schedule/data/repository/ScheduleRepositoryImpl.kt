package com.zc.bakamitai.feature.schedule.data.repository

import com.zc.bakamitai.common.data.mapper.toDomain
import com.zc.bakamitai.common.data.util.safeApiCall
import com.zc.bakamitai.common.domain.model.ScheduledShow
import com.zc.bakamitai.feature.schedule.data.mapper.toDomain
import com.zc.bakamitai.feature.schedule.data.remote.ScheduleService
import com.zc.bakamitai.feature.schedule.domain.repository.ScheduleRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import org.koin.core.annotation.Single
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * Keeps the last successful weekly schedule in memory for [WEEKLY_CACHE_TTL] per zone (docs/api.md §7): the
 * weekly template rarely changes, so a new-day reload costs only today's small request. Failures aren't
 * cached, and concurrent callers wait for the lock, so they share one request.
 */
@Single
class ScheduleRepositoryImpl(private val service: ScheduleService, private val clock: Clock) : ScheduleRepository {

    private val mutex = Mutex()
    private var cache: CachedWeek? = null

    override suspend fun getWeeklySchedule(
        timeZone: TimeZone,
        forceRefresh: Boolean,
    ): Result<Map<DayOfWeek, List<ScheduledShow>>> = mutex.withLock {
        val cached = cache?.takeIf {
            !forceRefresh && it.zoneId == timeZone.id && clock.now() - it.fetchedAt < WEEKLY_CACHE_TTL
        }
        if (cached != null) {
            Result.success(cached.days)
        } else {
            safeApiCall { service.getWeeklySchedule(timeZone.id).toDomain() }
                .onSuccess { days -> cache = CachedWeek(zoneId = timeZone.id, fetchedAt = clock.now(), days = days) }
        }
    }

    override suspend fun getTodaySchedule(timeZone: TimeZone): Result<List<ScheduledShow>> =
        safeApiCall { service.getTodaySchedule(timeZone.id).toDomain() }

    private class CachedWeek(val zoneId: String, val fetchedAt: Instant, val days: Map<DayOfWeek, List<ScheduledShow>>)

    private companion object {
        val WEEKLY_CACHE_TTL = 1.hours
    }
}
