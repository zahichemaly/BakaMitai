package com.zc.bakamitai.feature.settings.domain.usecase

import com.zc.bakamitai.common.domain.repository.SettingsRepository
import org.koin.core.annotation.Factory

/**
 * Saves the new-episode alerts switch. The release checks keep their schedule either way: with the switch off,
 * a check makes no request (docs/6-settings.md §5.1).
 */
@Factory
class UpdateNewEpisodeAlertsUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> = repository.setNewEpisodeAlerts(enabled)
}
