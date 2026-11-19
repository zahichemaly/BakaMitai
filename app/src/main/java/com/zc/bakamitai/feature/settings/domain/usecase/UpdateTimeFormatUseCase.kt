package com.zc.bakamitai.feature.settings.domain.usecase

import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.repository.SettingsRepository
import org.koin.core.annotation.Factory

/** Saves the clock every time is shown on. */
@Factory
class UpdateTimeFormatUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(timeFormat: TimeFormat): Result<Unit> = repository.setTimeFormat(timeFormat)
}
