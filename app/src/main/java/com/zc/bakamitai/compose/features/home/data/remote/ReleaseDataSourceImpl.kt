package com.zc.bakamitai.compose.features.home.data.remote

import com.zc.bakamitai.compose.core.network.AppClient
import com.zc.bakamitai.compose.features.home.data.model.LatestReleaseDTO
import com.zc.bakamitai.compose.features.home.data.model.TodayScheduleDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReleaseDataSourceImpl(private val appClient: AppClient) : ReleaseDataSource {
    override suspend fun getTodayReleases(): TodayScheduleDTO {
        return withContext(Dispatchers.IO) {
            appClient.get<TodayScheduleDTO>("/api/?f=schedule&h=true")
        }
    }

    override suspend fun getLatestReleases(): LatestReleaseDTO {
        return withContext(Dispatchers.IO) {
            appClient.get<LatestReleaseDTO>("/api/?f=latest")
        }
    }
}
