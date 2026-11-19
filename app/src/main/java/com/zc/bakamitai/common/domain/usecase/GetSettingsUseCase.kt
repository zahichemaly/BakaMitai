package com.zc.bakamitai.common.domain.usecase

import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

/** The user's settings right now, for one-off reads (Schedule's week start at each load). */
@Factory
class GetSettingsUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(): AppSettings = repository.observeSettings().first()
}
