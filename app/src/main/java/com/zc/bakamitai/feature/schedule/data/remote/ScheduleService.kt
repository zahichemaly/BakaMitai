package com.zc.bakamitai.feature.schedule.data.remote

import com.zc.bakamitai.common.data.dto.TodayScheduleDTO
import com.zc.bakamitai.feature.schedule.data.dto.WeeklyScheduleDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.koin.core.annotation.Single

/** SubsPlease's weekly and today's schedule (docs/api.md §3.1, §3.2). Every call sends `tz`. */
@Single
class ScheduleService(private val client: HttpClient) {

    /** The whole week, bucketed by weekday in [timeZoneId]. Without `tz` the body would be empty. */
    suspend fun getWeeklySchedule(timeZoneId: String): WeeklyScheduleDTO = client.get(API_PATH) {
        parameter(PARAM_FUNCTION, FUNCTION_SCHEDULE)
        parameter(PARAM_TIME_ZONE, timeZoneId)
    }.body()

    /** Today in [timeZoneId], with each show's `aired` flag. */
    suspend fun getTodaySchedule(timeZoneId: String): TodayScheduleDTO = client.get(API_PATH) {
        parameter(PARAM_FUNCTION, FUNCTION_SCHEDULE)
        parameter(PARAM_TODAY_ONLY, true)
        parameter(PARAM_TIME_ZONE, timeZoneId)
    }.body()

    private companion object {
        const val API_PATH = "api/"
        const val PARAM_FUNCTION = "f"
        const val PARAM_TODAY_ONLY = "h"
        const val PARAM_TIME_ZONE = "tz"
        const val FUNCTION_SCHEDULE = "schedule"
    }
}
