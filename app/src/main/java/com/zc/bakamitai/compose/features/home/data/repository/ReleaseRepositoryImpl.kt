package com.zc.bakamitai.compose.features.home.data.repository

import com.zc.bakamitai.compose.features.home.data.mapper.toDomain
import com.zc.bakamitai.compose.features.home.data.remote.ReleaseDataSource
import com.zc.bakamitai.compose.features.home.domain.model.LatestRelease
import com.zc.bakamitai.compose.features.home.domain.model.TodaySchedule
import com.zc.bakamitai.compose.features.home.domain.repository.ReleaseRepository

class ReleaseRepositoryImpl(private val releaseDataSource: ReleaseDataSource) : ReleaseRepository {
    override suspend fun getToday(): TodaySchedule {
        return releaseDataSource.getTodayReleases().toDomain()
    }

    override suspend fun getLatest(): LatestRelease {
        return releaseDataSource.getLatestReleases().toDomain()
    }
}
