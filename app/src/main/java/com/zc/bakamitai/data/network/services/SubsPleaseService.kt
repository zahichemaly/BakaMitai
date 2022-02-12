package com.zc.bakamitai.data.network.services

import com.zc.bakamitai.data.models.EntryResponse
import com.zc.bakamitai.data.models.ScheduleResponse
import com.zc.bakamitai.data.models.ShowEpisodeResponse
import com.zc.bakamitai.data.models.TodayScheduleResponse
import org.jsoup.nodes.Document
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SubsPleaseService {
    @GET("api/?f=latest")
    suspend fun getLatest(): Response<HashMap<String, EntryResponse>>

    @GET("api/?f=schedule")
    suspend fun getSchedule(): Response<ScheduleResponse>

    @GET("api/?f=schedule&h=true")
    suspend fun getTodaySchedule(): Response<TodayScheduleResponse>

    @GET("shows/{page}")
    suspend fun getShowDetails(@Path("page") page: String): Response<Document>

    @GET("shows")
    suspend fun getShows(): Response<Document>

    @GET("api/?f=show")
    suspend fun getShowEpisodes(@Query("sid") id: String): Response<ShowEpisodeResponse>
}
