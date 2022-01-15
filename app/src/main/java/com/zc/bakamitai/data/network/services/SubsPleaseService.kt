package com.zc.bakamitai.data.network.services

import com.zc.bakamitai.data.models.EntryResponse
import com.zc.bakamitai.data.models.ScheduleResponse
import retrofit2.Response
import retrofit2.http.GET

interface SubsPleaseService {
    @GET("api/?f=latest&tz=Asia/Beirut")
    suspend fun getLatest(): Response<HashMap<String, EntryResponse>>

    @GET("api/?f=schedule&tz=Asia/Beirut")
    suspend fun getSchedule(): Response<ScheduleResponse>
}
