package com.zc.bakamitai.compose.features.home.data.remote

import com.zc.bakamitai.compose.core.network.GenericResponse
import com.zc.bakamitai.compose.core.network.HttpClientWrapper
import com.zc.bakamitai.compose.features.home.data.model.LatestReleaseDTO
import com.zc.bakamitai.compose.features.home.data.model.TodayScheduleDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReleaseDataSourceImpl(private val httpClientWrapper: HttpClientWrapper) : ReleaseDataSource {
    override suspend fun getTodayReleases(): GenericResponse<TodayScheduleDTO> {
        return withContext(Dispatchers.IO) {
            httpClientWrapper.get<TodayScheduleDTO>("api/?f=schedule&h=true")
        }
    }

    override suspend fun getLatestReleases(): GenericResponse<LatestReleaseDTO> {
        return withContext(Dispatchers.IO) {
            httpClientWrapper.get<LatestReleaseDTO>("api/?f=latest")
        }
    }
}
