package com.zc.bakamitai.feature.home.domain.repository

import com.zc.bakamitai.feature.home.domain.model.Release
import com.zc.bakamitai.feature.home.domain.model.ScheduledShow
import kotlinx.datetime.TimeZone

/** SubsPlease's schedule and release feeds. Every call is made for [TimeZone] and fails with a `DomainError`. */
interface BroadcastRepository {

    /** Today's schedule in [timeZone], with times in that zone. */
    suspend fun getTodaySchedule(timeZone: TimeZone): Result<List<ScheduledShow>>

    /** A page (1 or 2) of the latest releases. A page past the end is an empty list. */
    suspend fun getLatestReleases(page: Int, timeZone: TimeZone): Result<List<Release>>

    /** Releases whose title matches [query]; an empty [query] returns the newest releases. */
    suspend fun searchReleases(query: String, timeZone: TimeZone): Result<List<Release>>
}
