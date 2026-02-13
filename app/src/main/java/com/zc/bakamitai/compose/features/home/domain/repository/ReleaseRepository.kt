package com.zc.bakamitai.compose.features.home.domain.repository

import com.zc.bakamitai.compose.core.domain.Resource
import com.zc.bakamitai.compose.features.home.domain.model.LatestRelease
import com.zc.bakamitai.compose.features.home.domain.model.TodaySchedule

interface ReleaseRepository {
    suspend fun getToday(): Resource<TodaySchedule>
    suspend fun getLatest(): Resource<LatestRelease>
}
