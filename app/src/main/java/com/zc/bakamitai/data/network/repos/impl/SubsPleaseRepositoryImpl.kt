package com.zc.bakamitai.data.network.repos.impl

import com.zc.bakamitai.data.models.EntryResponse
import com.zc.bakamitai.data.models.ScheduleResponse
import com.zc.bakamitai.data.models.TodayScheduleResponse
import com.zc.bakamitai.data.network.repos.SubsPleaseRepository
import com.zc.bakamitai.data.network.services.SubsPleaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.nodes.Document
import retrofit2.Response

class SubsPleaseRepositoryImpl(val subsPleaseService: SubsPleaseService) : SubsPleaseRepository {
    override suspend fun getLatest(): Response<HashMap<String, EntryResponse>> {
        return withContext(Dispatchers.IO) {
            subsPleaseService.getLatest()
        }
    }

    override suspend fun getSchedule(): Response<ScheduleResponse> {
        return withContext(Dispatchers.IO) {
            subsPleaseService.getSchedule()
        }
    }

    override suspend fun getTodaySchedule(): Response<TodayScheduleResponse> {
        return withContext(Dispatchers.IO) {
            subsPleaseService.getTodaySchedule()
        }
    }

    override suspend fun getShowDetails(page: String): Response<Document> {
        return withContext(Dispatchers.IO) {
            subsPleaseService.getShowDetails(page)
        }
    }
}
