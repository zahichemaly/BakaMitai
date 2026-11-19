package com.zc.bakamitai.feature.settings.domain.usecase

import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.repository.SettingsRepository
import org.koin.core.annotation.Factory

/** Saves the app's theme. */
@Factory
class UpdateThemeModeUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(themeMode: ThemeMode): Result<Unit> = repository.setThemeMode(themeMode)
}
