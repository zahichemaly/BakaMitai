package com.zc.bakamitai.data.network.services

import com.zc.bakamitai.data.models.EntryResponse
import com.zc.bakamitai.data.models.ScheduleResponse
import com.zc.bakamitai.data.models.TodayScheduleResponse
import retrofit2.Response
import retrofit2.http.GET

interface SubsPleaseService {
    @GET("api/?f=latest")
    suspend fun getLatest(): Response<HashMap<String, EntryResponse>>

    @GET("api/?f=schedule")
    suspend fun getSchedule(): Response<ScheduleResponse>

    @GET("api/?f=schedule&h=true")
    suspend fun getTodaySchedule(): Response<TodayScheduleResponse>
}
