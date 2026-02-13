package com.zc.bakamitai.compose.features.home.data.repository

import com.zc.bakamitai.compose.core.domain.Resource
import com.zc.bakamitai.compose.core.network.toResource
import com.zc.bakamitai.compose.features.home.data.mapper.toDomain
import com.zc.bakamitai.compose.features.home.data.model.TodayScheduleDTO
import com.zc.bakamitai.compose.features.home.data.remote.ReleaseDataSource
import com.zc.bakamitai.compose.features.home.domain.model.LatestRelease
import com.zc.bakamitai.compose.features.home.domain.model.Release
import com.zc.bakamitai.compose.features.home.domain.model.TodaySchedule
import com.zc.bakamitai.compose.features.home.domain.repository.ReleaseRepository
import com.zc.bakamitai.data.models.TodayScheduleResponse

class ReleaseRepositoryImpl(private val releaseDataSource: ReleaseDataSource) : ReleaseRepository {
    override suspend fun getToday(): Resource<TodaySchedule> {
        return releaseDataSource.getTodayReleases().toResource { it.toDomain() }
    }

    override suspend fun getLatest(): Resource<LatestRelease> {
        return releaseDataSource.getLatestReleases().toResource { it.toDomain() }
    }
}
