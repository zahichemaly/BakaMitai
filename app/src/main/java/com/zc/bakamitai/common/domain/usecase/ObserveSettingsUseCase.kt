package com.zc.bakamitai.common.domain.usecase

import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

/** The user's settings, then every change: the app root (theme, time format) and the Settings screen. */
@Factory
class ObserveSettingsUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<AppSettings> = repository.observeSettings()
}
