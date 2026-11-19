package com.zc.bakamitai.feature.settings.domain.usecase

import com.zc.bakamitai.common.domain.model.WeekStart
import com.zc.bakamitai.common.domain.repository.SettingsRepository
import org.koin.core.annotation.Factory

/** Saves the first day of the week (Schedule's tab order). */
@Factory
class UpdateWeekStartUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(weekStart: WeekStart): Result<Unit> = repository.setWeekStart(weekStart)
}
