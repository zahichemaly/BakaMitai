package com.zc.bakamitai.feature.home.data.remote

import com.zc.bakamitai.feature.home.data.dto.ReleaseListDTO
import com.zc.bakamitai.feature.home.data.dto.TodayScheduleDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.koin.core.annotation.Single

/** SubsPlease's schedule and release endpoints (docs/api.md §3.1, §3.3, §3.4). Every call sends `tz`. */
@Single
class BroadcastService(private val client: HttpClient) {

    suspend fun getTodaySchedule(timeZoneId: String): TodayScheduleDTO = client.get(API_PATH) {
        parameter(PARAM_FUNCTION, FUNCTION_SCHEDULE)
        parameter(PARAM_TODAY_ONLY, true)
        parameter(PARAM_TIME_ZONE, timeZoneId)
    }.body()

    /** [page] is 1 or 2; later pages answer `limit_reached`. */
    suspend fun getLatestReleases(timeZoneId: String, page: Int): ReleaseListDTO = client.get(API_PATH) {
        parameter(PARAM_FUNCTION, FUNCTION_LATEST)
        parameter(PARAM_TIME_ZONE, timeZoneId)
        parameter(PARAM_PAGE, page)
    }.body()

    /** An empty [query] (sent as `s=`) returns the newest releases. */
    suspend fun searchReleases(timeZoneId: String, query: String): ReleaseListDTO = client.get(API_PATH) {
        parameter(PARAM_FUNCTION, FUNCTION_SEARCH)
        parameter(PARAM_TIME_ZONE, timeZoneId)
        parameter(PARAM_SEARCH, query)
    }.body()

    private companion object {
        const val API_PATH = "api/"
        const val PARAM_FUNCTION = "f"
        const val PARAM_TODAY_ONLY = "h"
        const val PARAM_TIME_ZONE = "tz"
        const val PARAM_PAGE = "p"
        const val PARAM_SEARCH = "s"
        const val FUNCTION_SCHEDULE = "schedule"
        const val FUNCTION_LATEST = "latest"
        const val FUNCTION_SEARCH = "search"
    }
}
