package com.zc.bakamitai.data.network.repos

import com.zc.bakamitai.data.models.EntryResponse
import com.zc.bakamitai.data.models.ScheduleResponse
import com.zc.bakamitai.data.models.ShowEpisodeResponse
import com.zc.bakamitai.data.models.TodayScheduleResponse
import org.jsoup.nodes.Document
import retrofit2.Response

interface SubsPleaseRepository {
    suspend fun getLatest(): Response<HashMap<String, EntryResponse>>
    suspend fun getSchedule(): Response<ScheduleResponse>
    suspend fun getTodaySchedule(): Response<TodayScheduleResponse>
    suspend fun getShowDetails(page: String): Response<Document>
    suspend fun getShows(): Response<Document>
    suspend fun getShowEpisodes(id: String): Response<ShowEpisodeResponse>
}
