package com.zc.bakamitai.compose.features.home.data.remote

import com.zc.bakamitai.compose.features.home.data.model.LatestReleaseDTO
import com.zc.bakamitai.compose.features.home.data.model.TodayScheduleDTO

interface ReleaseDataSource {
    suspend fun getTodayReleases(): TodayScheduleDTO
    suspend fun getLatestReleases(): LatestReleaseDTO
}
