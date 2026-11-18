package com.zc.bakamitai.feature.home.data.repository

import com.zc.bakamitai.common.data.mapper.toDomain
import com.zc.bakamitai.common.data.util.safeApiCall
import com.zc.bakamitai.common.domain.model.ScheduledShow
import com.zc.bakamitai.feature.home.data.mapper.toDomain
import com.zc.bakamitai.feature.home.data.remote.BroadcastService
import com.zc.bakamitai.feature.home.domain.model.Release
import com.zc.bakamitai.feature.home.domain.repository.BroadcastRepository
import kotlinx.datetime.TimeZone
import org.koin.core.annotation.Single

@Single
class BroadcastRepositoryImpl(private val service: BroadcastService) : BroadcastRepository {

    override suspend fun getTodaySchedule(timeZone: TimeZone): Result<List<ScheduledShow>> =
        safeApiCall { service.getTodaySchedule(timeZone.id).toDomain() }

    override suspend fun getLatestReleases(page: Int, timeZone: TimeZone): Result<List<Release>> =
        safeApiCall { service.getLatestReleases(timeZone.id, page).toDomain() }

    override suspend fun searchReleases(query: String, timeZone: TimeZone): Result<List<Release>> =
        safeApiCall { service.searchReleases(timeZone.id, query).toDomain() }
}
