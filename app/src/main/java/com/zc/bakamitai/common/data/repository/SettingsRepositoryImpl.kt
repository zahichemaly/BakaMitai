package com.zc.bakamitai.common.data.repository

import com.zc.bakamitai.common.data.local.SettingsLocalDataSource
import com.zc.bakamitai.common.data.util.safeDataStoreCall
import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.model.WeekStart
import com.zc.bakamitai.common.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

/** The settings from the local DataStore; every write is wrapped in [safeDataStoreCall]. */
@Single
class SettingsRepositoryImpl(private val localDataSource: SettingsLocalDataSource) : SettingsRepository {

    override fun observeSettings(): Flow<AppSettings> = localDataSource.settings

    override suspend fun setWeekStart(value: WeekStart): Result<Unit> =
        safeDataStoreCall { localDataSource.setWeekStart(value) }

    override suspend fun setTimeFormat(value: TimeFormat): Result<Unit> =
        safeDataStoreCall { localDataSource.setTimeFormat(value) }

    override suspend fun setThemeMode(value: ThemeMode): Result<Unit> =
        safeDataStoreCall { localDataSource.setThemeMode(value) }

    override suspend fun setNewEpisodeAlerts(enabled: Boolean): Result<Unit> =
        safeDataStoreCall { localDataSource.setNewEpisodeAlerts(enabled) }
}
