package com.zc.bakamitai.common.domain.repository

import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.model.WeekStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.IOException

/**
 * In-memory settings: [settings] is what observers see (tests may set it, as another screen's write would). A write
 * updates it and is recorded in [writes], unless [failWrites] makes it fail like a full disk.
 */
class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {

    val settings = MutableStateFlow(initial)
    var failWrites = false
    val writes = mutableListOf<Any>()

    override fun observeSettings(): Flow<AppSettings> = settings

    override suspend fun setWeekStart(value: WeekStart): Result<Unit> = write(value) { it.copy(weekStart = value) }

    override suspend fun setTimeFormat(value: TimeFormat): Result<Unit> = write(value) { it.copy(timeFormat = value) }

    override suspend fun setThemeMode(value: ThemeMode): Result<Unit> = write(value) { it.copy(themeMode = value) }

    override suspend fun setNewEpisodeAlerts(enabled: Boolean): Result<Unit> =
        write(enabled) { it.copy(newEpisodeAlerts = enabled) }

    private fun write(value: Any, transform: (AppSettings) -> AppSettings): Result<Unit> {
        if (failWrites) return Result.failure(DomainError.Unknown(IOException("disk full")))
        writes += value
        settings.update(transform)
        return Result.success(Unit)
    }
}
