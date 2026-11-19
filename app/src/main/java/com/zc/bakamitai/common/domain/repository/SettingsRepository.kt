package com.zc.bakamitai.common.domain.repository

import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.model.WeekStart
import kotlinx.coroutines.flow.Flow

/** The user's preferences. Reads never fail (a read error gives the defaults); a failed write is a `DomainError`. */
interface SettingsRepository {

    /** The current settings, then every change. */
    fun observeSettings(): Flow<AppSettings>

    suspend fun setWeekStart(value: WeekStart): Result<Unit>

    suspend fun setTimeFormat(value: TimeFormat): Result<Unit>

    suspend fun setThemeMode(value: ThemeMode): Result<Unit>

    suspend fun setNewEpisodeAlerts(enabled: Boolean): Result<Unit>
}
